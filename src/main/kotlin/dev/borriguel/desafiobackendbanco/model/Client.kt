package dev.borriguel.desafiobackendbanco.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "clients")
data class Client(
    @Id
    val id: Long? = null,
    private var email: String,
    private var password: String,
    private var walletId: Long
) {
    init {
        validateEmail(email)
        validatePassword(password)
    }

    fun getEmail(): String = email
    fun getWallet(): Long = walletId

    fun setWallet(walletId: Long) {
        this.walletId = walletId
    }

    fun changeEmail(newEmail: String) {
        validateEmail(newEmail)
        this.email = newEmail
    }

    fun changePassword(newPassword: String) {
        validatePassword(newPassword)
        this.password = newPassword
    }

    fun validateEmail(email: String) {
        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)\$"))) {
            throw IllegalArgumentException("Invalid email: $email")
        }
    }

    fun validatePassword(password: String) {
        if (password.length < 6) throw IllegalArgumentException("Password must be at least 6 characters long")
    }
}
