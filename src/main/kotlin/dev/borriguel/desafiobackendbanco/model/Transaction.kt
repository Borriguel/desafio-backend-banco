package dev.borriguel.desafiobackendbanco.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table(name = "transactions")
data class Transaction private constructor(
    @Id
    val id: Long? = null,
    private val payerId: Long,
    private val payeeId: Long,
    @Column("transaction_value")
    private val value: BigDecimal,
    @Column("idempotency_key")
    private val idempotencyKey: String,
    private var status: Status,
    private val createdAt: LocalDateTime = LocalDateTime.now(),
    private var authorizedAt: LocalDateTime? = null,
    private var failedAt: LocalDateTime? = null
) {
    companion object {
        fun create(payerId: Long, payeeId: Long, value: BigDecimal, idempotencyKey: String): Transaction {
            require(payerId != payeeId) { throw IllegalArgumentException("Payer and payee cannot be the same") }
            require(value > BigDecimal.ZERO) { throw IllegalArgumentException("Value must be greater than zero") }
            require(idempotencyKey.isNotBlank()) { throw IllegalArgumentException("Idempotency key cannot be blank") }
            return Transaction(
                payerId = payerId,
                payeeId = payeeId,
                value = value,
                idempotencyKey = idempotencyKey,
                status = Status.CREATED,
                createdAt = LocalDateTime.now(),
            )
        }
    }

    fun authorize() {
        ensureStatus(Status.CREATED)
        status = Status.AUTHORIZED
        authorizedAt = LocalDateTime.now()
    }

    fun fail() {
        ensureStatus(Status.CREATED)
        status = Status.FAILED
        failedAt = LocalDateTime.now()
    }

    private fun ensureStatus(expected: Status) {
        require(status == expected) { throw IllegalArgumentException("Invalid transaction status") }
    }

    fun getPayerId(): Long = payerId
    fun getPayeeId(): Long = payeeId
    fun getValue(): BigDecimal = value
    fun getIdempotencyKey(): String = idempotencyKey
    fun getStatus(): Status = status
    fun getCreatedAt(): LocalDateTime = createdAt
    fun getAuthorizedAt(): LocalDateTime? = authorizedAt
    fun getFailedAt(): LocalDateTime? = failedAt
}
