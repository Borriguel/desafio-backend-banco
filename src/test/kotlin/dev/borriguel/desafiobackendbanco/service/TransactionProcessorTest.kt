package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.exception.InsufficientFundsException
import dev.borriguel.desafiobackendbanco.model.Transaction
import dev.borriguel.desafiobackendbanco.model.Wallet
import dev.borriguel.desafiobackendbanco.repository.TransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class TransactionProcessorTest {

    @Mock
    private lateinit var repository: TransactionRepository

    @Mock
    private lateinit var walletService: WalletService

    @Mock
    private lateinit var authorizationService: AuthorizationService

    @InjectMocks
    private lateinit var transactionProcessor: TransactionProcessor

    @Test
    fun `processWithRetry should succeed on the first attempt when everything is correct`() = runTest {
        val transaction = mock<Transaction>()
        val payerWallet = mock<Wallet>()
        val payeeWallet = mock<Wallet>()

        whenever(transaction.getPayerId()).thenReturn(1L)
        whenever(transaction.getPayeeId()).thenReturn(2L)
        whenever(transaction.getValue()).thenReturn(BigDecimal(100))
        whenever(authorizationService.authorize()).thenReturn(true)
        whenever(walletService.getById(1L)).thenReturn(payerWallet)
        whenever(walletService.getById(2L)).thenReturn(payeeWallet)
        whenever(repository.save(any())).thenReturn(transaction)

        transactionProcessor.processWithRetry(transaction)

        inOrder(repository, walletService, authorizationService, transaction, payerWallet) {
            verify(transaction).process()
            verify(repository).save(transaction)
            verify(authorizationService).authorize()
            verify(walletService).getById(1L)
            verify(walletService).getById(2L)
            verify(payerWallet).transferTo(payeeWallet, BigDecimal(100))
            verify(walletService).updateBoth(payerWallet, payeeWallet)
            verify(transaction).authorize()
            verify(repository).save(transaction)
        }

        verify(transaction, never()).fail()
    }

    @Test
    fun `processWithRetry should fail transaction on InsufficientFundsException and not retry`() = runTest {
        val transaction = mock<Transaction>()
        val payerWallet = mock<Wallet>()
        val payeeWallet = mock<Wallet>()

        whenever(transaction.getPayerId()).thenReturn(1L)
        whenever(transaction.getPayeeId()).thenReturn(2L)
        whenever(transaction.getValue()).thenReturn(BigDecimal(100))
        whenever(authorizationService.authorize()).thenReturn(true)
        whenever(walletService.getById(1L)).thenReturn(payerWallet)
        whenever(walletService.getById(2L)).thenReturn(payeeWallet)
        whenever(payerWallet.transferTo(any(), any())).thenThrow(InsufficientFundsException("Not enough funds"))
        whenever(repository.save(any())).thenReturn(transaction)

        transactionProcessor.processWithRetry(transaction)

        verify(authorizationService, times(1)).authorize()
        verify(transaction).fail()
        verify(repository, times(2)).save(transaction) // 1 para process, 1 para fail
        verify(transaction, never()).authorize()
        verify(walletService, never()).updateBoth(any(), any())
    }

    @Test
    fun `processWithRetry should fail after all retries if authorization always fails`() = runTest {
        val transaction = mock<Transaction>()
        val retryCount = 3

        whenever(authorizationService.authorize()).thenReturn(false)
        whenever(repository.save(any())).thenReturn(transaction)

        transactionProcessor.processWithRetry(transaction, retryCount = retryCount)

        verify(authorizationService, times(retryCount)).authorize()
        verify(transaction).fail()
        verify(repository, times(2)).save(transaction)
        verify(walletService, never()).getById(any())
        verify(transaction, never()).authorize()
    }

    @Test
    fun `processWithRetry should succeed on the second attempt`() = runTest {
        val transaction = mock<Transaction>()
        val payerWallet = mock<Wallet>()
        val payeeWallet = mock<Wallet>()

        whenever(transaction.getPayerId()).thenReturn(1L)
        whenever(transaction.getPayeeId()).thenReturn(2L)
        whenever(transaction.getValue()).thenReturn(BigDecimal(50))
        whenever(authorizationService.authorize()).thenReturn(false, true)
        whenever(walletService.getById(1L)).thenReturn(payerWallet)
        whenever(walletService.getById(2L)).thenReturn(payeeWallet)
        whenever(repository.save(any())).thenReturn(transaction)

        transactionProcessor.processWithRetry(transaction, retryCount = 3)

        verify(authorizationService, times(2)).authorize()
        verify(transaction).authorize()
        verify(repository, times(2)).save(transaction)
        verify(payerWallet).transferTo(eq(payeeWallet), any())
        verify(walletService).updateBoth(payerWallet, payeeWallet)
        verify(transaction, never()).fail()
    }

    @Test
    fun `processWithRetry should retry on generic exception and fail if it persists`() = runTest {
        val transaction = mock<Transaction>()
        val retryCount = 2

        whenever(authorizationService.authorize()).thenThrow(RuntimeException("External service down"))
        whenever(repository.save(any())).thenReturn(transaction)

        transactionProcessor.processWithRetry(transaction, retryCount = retryCount)

        verify(authorizationService, times(retryCount)).authorize()
        verify(transaction).fail()
        verify(repository, times(2)).save(transaction)
        verify(walletService, never()).getById(any())
        verify(transaction, never()).authorize()
    }
}
