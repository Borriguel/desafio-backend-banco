package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.dto.AuthorizationDto
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class AuthorizationService(private val webClient: WebClient) {
    suspend fun authorize(): Boolean {
        return try {
            val response = webClient
                .get()
                .uri("https://util.devi.tools/api/v2/authorize")
                .header("Accept","application/json")
                .retrieve()
                .awaitBody<AuthorizationDto>()
            response.data.authorization
        } catch (e: Exception) {
            false
        }
    }
}
