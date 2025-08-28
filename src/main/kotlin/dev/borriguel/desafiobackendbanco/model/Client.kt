package dev.borriguel.desafiobackendbanco.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "clients")
data class Client(
    @Id
    val id: Long? = null,
    private var email: String,
    private var name: String,
    private var document: String,
    private var password: String,
    private var walletId: Long
) {
    init {
        validateEmail(email)
        validatePassword(password)
        validateName(name)
        validateDocument(document)
    }

    fun getEmail(): String = email
    fun getWallet(): Long = walletId
    fun getName(): String = name
    fun getDocument(): String = document

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

    fun changeName(newName: String) {
        validateName(newName)
        this.name = newName
    }

    fun changeDocument(newDocument: String) {
        validateDocument(newDocument)
        this.document = newDocument
    }

    fun validateEmail(email: String) {
        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)\$"))) {
            throw IllegalArgumentException("Invalid email: $email")
        }
    }

    fun validatePassword(password: String) {
        if (password.length < 6) throw IllegalArgumentException("Password must be at least 6 characters long")
    }

    fun validateName(name: String) {
        if (name.length < 3) throw IllegalArgumentException("Name must be at least 3 characters long")
        if (name.length > 100) throw IllegalArgumentException("Name cannot exceed 100 characters")
    }

    fun validateDocument(document: String) {
        if (document.length != 11) throw IllegalArgumentException("Document must be 11 characters long")
        if (!document.matches(Regex("^[0-9]{11}\$"))) throw IllegalArgumentException("Invalid document")
    }
}
