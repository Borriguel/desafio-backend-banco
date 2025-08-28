package dev.borriguel.desafiobackendbanco.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime

data class ApiErrorDto(
    val timestamp: OffsetDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val details: List<String>? = null

)
