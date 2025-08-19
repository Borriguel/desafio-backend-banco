package dev.borriguel.desafiobackendbanco.model

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
data class Wallet(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    private var balance: BigDecimal = BigDecimal.ZERO,
    @Enumerated(EnumType.STRING)
    private val type: AccountType
) {
    init {
        if (balance < BigDecimal.ZERO) throw IllegalArgumentException("Initial balance cannot be negative")
    }

    fun getBalance(): BigDecimal = balance
    fun getType(): AccountType = type

    fun deposit(amount: BigDecimal) {
        validateAmount(amount)
        balance.add(amount)
    }

    fun withdraw(amount: BigDecimal) {
        validateAmount(amount)
        if (!hasSufficientBalance(amount)) throw IllegalArgumentException("Insufficient balance")
        balance.subtract(amount)
    }

    fun transferTo(targetWallet: Wallet, amount: BigDecimal) {
        if (type == AccountType.SHOPKEEPER) throw IllegalArgumentException("Shopkeeper account cannot do transactions")
        withdraw(amount)
        targetWallet.deposit(amount)
    }

    private fun validateAmount(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) throw IllegalArgumentException("Amount must be above zero")
    }

    private fun hasSufficientBalance(amount: BigDecimal): Boolean {
        return balance >= amount
    }
}
