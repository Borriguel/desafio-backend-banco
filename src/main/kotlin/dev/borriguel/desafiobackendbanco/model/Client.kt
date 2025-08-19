package dev.borriguel.desafiobackendbanco.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
data class Client(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    private var email: String,
    @Column(nullable = false)
    private var password: String,
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = false)
    private val wallet: Wallet
) {
    init {
        validateEmail(email)
        validatePassword(password)
    }

    fun getEmail(): String = email
    fun getWallet(): Wallet = wallet

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
