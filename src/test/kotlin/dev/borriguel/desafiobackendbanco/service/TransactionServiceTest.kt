package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.exception.NotFoundException
import dev.borriguel.desafiobackendbanco.model.AccountType
import dev.borriguel.desafiobackendbanco.model.Status
import dev.borriguel.desafiobackendbanco.model.Transaction
import dev.borriguel.desafiobackendbanco.model.Wallet
import dev.borriguel.desafiobackendbanco.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(MockitoExtension::class)
class TransactionServiceTest {
    @InjectMocks
    private lateinit var service: TransactionService
    @Mock
    private lateinit var repository: TransactionRepository
    @Mock
    private lateinit var walletService: WalletService
    @Mock
    private lateinit var transactionProcessor: TransactionProcessor

    @Test
    fun `createTransaction should return existing transaction when idempotency key exists`() = runTest {
        val payerId = 1L
        val payeeId = 2L
        val value = BigDecimal("100.00")
        val idempotencyKey = "key123"
        val existingTransaction = Transaction.create(payerId, payeeId, value, idempotencyKey)

        whenever(repository.findByIdempotencyKey(eq(idempotencyKey))).
        thenReturn(existingTransaction)

        val result = service.createTransaction(payerId, payeeId, value, idempotencyKey)

        assertEquals(existingTransaction, result)
        verify(repository).findByIdempotencyKey(eq(idempotencyKey))
        verify(repository, never()).save(any())
        verify(walletService, never()).getById(any())
    }

    @Test
    fun `createTransaction should create new transaction when idempotency key does not exist`() = runTest {
        val payerId = 1L
        val payeeId = 2L
        val value = BigDecimal("100.00")
        val idempotencyKey = "key123"
        val payerWallet = Wallet(id = payerId, balance = BigDecimal("500.00"), type = AccountType.COMMON)
        val payeeWallet = Wallet(id = payeeId, balance = BigDecimal.ZERO, type = AccountType.SHOPKEEPER)

        whenever(repository.findByIdempotencyKey(eq(idempotencyKey))).thenReturn(null)
        whenever(walletService.getById(eq(payerId))).thenReturn(payerWallet)
        whenever(walletService.getById(eq(payeeId))).thenReturn(payeeWallet)
        whenever(repository.save(any())).thenAnswer { it.getArgument<Transaction>(0) }

        val result = service.createTransaction(payerId, payeeId, value, idempotencyKey)

        verify(repository).findByIdempotencyKey(eq(idempotencyKey))
        verify(walletService).getById(eq(payerId))
        verify(walletService).getById(eq(payeeId))

        val txCaptor = argumentCaptor<Transaction>()
        verify(repository).save(txCaptor.capture())
        val persisted = txCaptor.firstValue
        assertEquals(payerId, persisted.getPayerId())
        assertEquals(payeeId, persisted.getPayeeId())
        assertEquals(value, persisted.getValue())
        assertEquals(idempotencyKey, persisted.getIdempotencyKey())
        assertEquals(Status.CREATED, persisted.getStatus())
        assertEquals(persisted, result)
    }

    @Test
    fun `getById should return transaction when found`() = runTest {
        val transactionId = 42L
        val tx = Transaction.create(1L, 2L, BigDecimal("50.00"), "idem-42")

        whenever(repository.findById(eq(transactionId))).thenReturn(tx)

        val result = service.getById(transactionId)

        assertEquals(tx, result)
        verify(repository).findById(eq(transactionId))
    }

    @Test
    fun `getById should throw NotFoundException when transaction not found`() = runTest {
        val transactionId = 1L
        whenever(repository.findById(eq(transactionId))).thenReturn(null)

        val exception = assertThrows<NotFoundException> {
            service.getById(transactionId)
        }

        assertEquals("Transaction not found", exception.message)
        verify(repository).findById(eq(transactionId))
    }

    @Test
    fun `getAllByPayerId should return transactions for given payer`() = runTest {
        val payerId = 1L
        val transactions = listOf(
            Transaction.create(payerId, 2L, BigDecimal("100.00"), "key1"),
            Transaction.create(payerId, 3L, BigDecimal("200.00"), "key2")
        )
        whenever(repository.findByPayerId(eq(payerId))).thenReturn(transactions)

        val result = service.getAllByPayerId(payerId)

        assertEquals(transactions, result)
        verify(repository).findByPayerId(eq(payerId))
    }

    @Test
    fun `getAllByPayeeId should return transactions for given payee`() = runTest {
        val payeeId = 2L
        val transactions = listOf(
            Transaction.create(1L, payeeId, BigDecimal("100.00"), "key1"),
            Transaction.create(3L, payeeId, BigDecimal("200.00"), "key2")
        )
        whenever(repository.findByPayeeId(eq(payeeId))).thenReturn(transactions)

        val result = service.getAllByPayeeId(payeeId)

        assertEquals(transactions, result)
        verify(repository).findByPayeeId(eq(payeeId))
    }

    @Test
    fun `processPendingPayment should process all pending transactions`() = runTest {
        val pendingTransactions = listOf(
            Transaction.create(1L, 2L, BigDecimal("100.00"), "key1"),
            Transaction.create(3L, 4L, BigDecimal("200.00"), "key2")
        )
        whenever(repository.findByStatus(eq(Status.CREATED))).thenReturn(pendingTransactions)

        service.processPendingPayment()

        verify(repository).findByStatus(eq(Status.CREATED))
        verify(transactionProcessor, times(pendingTransactions.size)).processWithRetry(any(), any())
    }

    @Test
    fun `processPendingPayment should handle empty pending transactions`() = runTest {
        whenever(repository.findByStatus(eq(Status.CREATED))).thenReturn(emptyList())

        service.processPendingPayment()

        verify(repository).findByStatus(eq(Status.CREATED))
        verify(transactionProcessor, never()).processWithRetry(any(), any())
    }
}
