package dev.borriguel.desafiobackendbanco.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class WalletOperationsDto(
    @field:NotNull(message = "Amount cannot be null")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    val amount: BigDecimal
)
