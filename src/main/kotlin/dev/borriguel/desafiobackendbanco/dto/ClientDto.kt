package dev.borriguel.desafiobackendbanco.dto

import dev.borriguel.desafiobackendbanco.model.AccountType
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
    
    @field:NotNull(message = "Account type cannot be null")
    val accountType: AccountType
)
