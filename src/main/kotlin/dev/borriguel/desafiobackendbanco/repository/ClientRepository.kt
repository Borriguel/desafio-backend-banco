package dev.borriguel.desafiobackendbanco.repository

import dev.borriguel.desafiobackendbanco.model.Client
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ClientRepository: CoroutineCrudRepository<Client, Long> {
    suspend fun findByEmail(email: String): Client?
    suspend fun existsByEmail(email: String): Boolean
}
