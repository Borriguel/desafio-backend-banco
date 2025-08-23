package dev.borriguel.desafiobackendbanco.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table(name = "wallets")
data class Wallet(
    @Id
    val id: Long? = null,
    private var balance: BigDecimal = BigDecimal.ZERO,
    private val type: AccountType
) {
    init {
        if (balance < BigDecimal.ZERO) throw IllegalArgumentException("Initial balance cannot be negative")
    }

    fun getBalance(): BigDecimal = balance
    fun getType(): AccountType = type

    fun deposit(amount: BigDecimal) {
        validateAmount(amount)
        balance = balance.add(amount)
    }

    fun withdraw(amount: BigDecimal) {
        validateAmount(amount)
        if (!hasSufficientBalance(amount)) throw IllegalArgumentException("Insufficient balance")
        balance = balance.subtract(amount)
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
