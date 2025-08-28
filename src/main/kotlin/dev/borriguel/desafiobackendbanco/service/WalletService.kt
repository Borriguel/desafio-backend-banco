package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.model.AccountType
import dev.borriguel.desafiobackendbanco.model.Wallet
import dev.borriguel.desafiobackendbanco.repository.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class WalletService(private val repository: WalletRepository) {
    @Transactional
    suspend fun createWallet(accountType: AccountType) : Wallet {
        return repository.save(Wallet(
            type = accountType
        ))
    }

    suspend fun getById(id: Long) : Wallet {
        return repository.findById(id) ?: throw IllegalArgumentException("Wallet not found")
    }

    @Transactional
    suspend fun depositById(id: Long, amount: BigDecimal) : Wallet {
        val wallet = getById(id)
        wallet.deposit(amount)
        repository.save(wallet)
        return wallet
    }

    @Transactional
    suspend fun withdrawById(id: Long, amount: BigDecimal) : Wallet {
        val wallet = getById(id)
        wallet.withdraw(amount)
        repository.save(wallet)
        return wallet
    }

    @Transactional
    suspend fun updateBoth(payer: Wallet, payee: Wallet) {
        repository.save(payer)
        repository.save(payee)
    }
}
