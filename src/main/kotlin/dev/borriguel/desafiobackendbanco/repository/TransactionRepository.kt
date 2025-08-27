package dev.borriguel.desafiobackendbanco.repository

import dev.borriguel.desafiobackendbanco.model.Status
import dev.borriguel.desafiobackendbanco.model.Transaction
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TransactionRepository: CoroutineCrudRepository<Transaction, Long> {
    suspend fun findByStatus(status: Status): List<Transaction>
    suspend fun findByIdempotencyKey(idempotencyKey: String): Transaction?
}
