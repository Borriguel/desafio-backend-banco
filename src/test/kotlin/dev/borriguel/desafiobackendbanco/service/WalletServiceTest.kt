package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.exception.NotFoundException
import dev.borriguel.desafiobackendbanco.model.AccountType
import dev.borriguel.desafiobackendbanco.model.Wallet
import dev.borriguel.desafiobackendbanco.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(MockitoExtension::class)
class WalletServiceTest {
    @Mock
    private lateinit var repository: WalletRepository
    @InjectMocks
    private lateinit var service: WalletService

    @Test
    fun `createWallet should create a new wallet`() = runTest {
        val wallet = Wallet(type = AccountType.SHOPKEEPER)
        whenever(repository.save(any<Wallet>())).thenReturn(wallet)
        val result = service.createWallet(AccountType.SHOPKEEPER)
        assertEquals(wallet, result)
    }

    @Test
    fun `getById should return a wallet when found`() = runTest {
        val walletId = 1L
        val expectedWallet = Wallet(id = walletId, BigDecimal.ZERO, type = AccountType.SHOPKEEPER)
        whenever(repository.findById(walletId)).thenReturn(expectedWallet)
        val result = service.getById(walletId)
        assertEquals(expectedWallet, result)
    }

    @Test
    fun `getById should throw NotFoundException when wallet not exists`() = runTest {
        val id = 1L
        whenever(repository.findById(id)).thenReturn(null)
        val exception = assertThrows<NotFoundException> { service.getById(id) }
        assertEquals("Wallet not found", exception.message)
        verify(repository).findById(id)
    }

    @Test
    fun `depositById should update wallet balance`() = runTest {
        val walletId = 1L
        val depositAmount = BigDecimal("145.02")
        val wallet = Wallet(id = walletId, BigDecimal.ZERO, type = AccountType.SHOPKEEPER)
        val expectedBalance = BigDecimal("145.02")
        whenever(repository.findById(walletId)).thenReturn(wallet)
        whenever(repository.save(any<Wallet>())).thenReturn(wallet)
        val result = service.depositById(walletId, depositAmount)
        assertEquals(expectedBalance, result.getBalance())
    }

    @Test
    fun `depositById should throw NotFoundException when wallet not exists`() = runTest {
        val id = 1L
        whenever(repository.findById(id)).thenReturn(null)
        val exception = assertThrows<NotFoundException> { service.depositById(id, BigDecimal("122")) }
        assertEquals("Wallet not found", exception.message)
        verify(repository).findById(id)
    }

    @Test
    fun `withdrawById should update wallet balance`() = runTest {
        val walletId = 1L
        val withdrawAmount = BigDecimal("846.09")
        val wallet = Wallet(id = walletId, BigDecimal("1000.00"), type = AccountType.SHOPKEEPER)
        val expectedBalance = BigDecimal("153.91")
        whenever(repository.findById(walletId)).thenReturn(wallet)
        whenever(repository.save(any<Wallet>())).thenReturn(wallet)
        val result = service.withdrawById(walletId, withdrawAmount)
        assertEquals(expectedBalance, result.getBalance())
    }

    @Test
    fun `withdrawById should throw NotFoundException when wallet not exists`() = runTest {
        val walletId = 1L
        val withdrawAmount = BigDecimal("846.09")
        whenever(repository.findById(walletId)).thenReturn(null)
        val exception = assertThrows<NotFoundException> { service.withdrawById(walletId, withdrawAmount) }
        assertEquals("Wallet not found", exception.message)
        verify(repository).findById(walletId)
    }

    @Test
    fun `updateBoth should update both wallets`() = runTest {
        val payeeWallet = Wallet(type = AccountType.SHOPKEEPER)
        val payerWallet = Wallet(type = AccountType.COMMON)
        service.updateBoth(payerWallet, payeeWallet)
        verify(repository).save(payerWallet)
        verify(repository).save(payeeWallet)
    }
}
