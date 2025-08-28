package dev.borriguel.desafiobackendbanco.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class TransactionDto(
    @field:NotNull(message = "Payer cannot be null")
    val payerId: Long,
    @field:NotNull(message = "Amount cannot be null")
    @field:DecimalMin(value = "0.01", message = "Value must be greater than zero")
    val value: BigDecimal,
    @field:NotNull(message = "Payee cannot be null")
    val payeeId: Long)
