package dev.borriguel.desafiobackendbanco.dto

import dev.borriguel.desafiobackendbanco.model.AccountType
import dev.borriguel.desafiobackendbanco.model.Wallet
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ClientDto(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email should be valid")
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 6, message = "Password must be at least 6 characters long")
    val password: String,

    @field:NotBlank(message = "Name cannot be blank")
    @field:Size(max = 100, message = "Name must be at most 100 characters long")
    val name: String,

    @field:NotBlank(message = "Document cannot be blank")
    @field:Size(min = 11, max = 11, message = "Document must be 11 characters long")
    val document: String,

    @field:NotNull(message = "Account type cannot be null")
    val accountType: AccountType
)

data class ClientWalletDto(
    val id: Long,
    val name: String,
    val document: String,
    val wallet: Wallet
)
