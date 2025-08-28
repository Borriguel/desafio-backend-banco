package dev.borriguel.desafiobackendbanco.dto

data class AuthorizationDto(
    val status: String,
    val data: AuthorizationData
)

data class AuthorizationData(
    val authorization: Boolean
)
