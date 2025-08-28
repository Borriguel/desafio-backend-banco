package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.exception.NotFoundException
import dev.borriguel.desafiobackendbanco.model.Status
import dev.borriguel.desafiobackendbanco.model.Transaction
import dev.borriguel.desafiobackendbanco.repository.TransactionRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class TransactionService(
    private val repository: TransactionRepository,
    private val walletService: WalletService,
    private val transactionProcessor: TransactionProcessor) {
    val logger: Logger = LoggerFactory.getLogger(TransactionService::class.java)

    @Transactional
    suspend fun createTransaction(payerId: Long, payeeId: Long, value: BigDecimal, idempotencyKey: String): Transaction {
        val existing = repository.findByIdempotencyKey(idempotencyKey)
        if (existing != null) {
            logger.info("Transaction with idempotency key $idempotencyKey already exists with id ${existing.id}")
            return existing
        }
        val payer = walletService.getById(payerId)
        payer.validateShopkeeperTransaction()
        walletService.getById(payeeId)
        val transaction = Transaction.create(payerId, payeeId, value, idempotencyKey)
        logger.info("Created transaction ${transaction.getPayeeId()} -> ${transaction.getPayerId()} with value ${transaction.getValue()}")
        return repository.save(transaction)
    }

    suspend fun getById(id: Long): Transaction {
        return repository.findById(id) ?: throw NotFoundException("Transaction not found")
    }

    suspend fun getAllByPayerId(payerId: Long): List<Transaction> {
        return repository.findByPayerId(payerId)
    }

    suspend fun getAllByPayeeId(payeeId: Long): List<Transaction> {
        return repository.findByPayeeId(payeeId)
    }

    @Scheduled(fixedDelay = 15000)
    suspend fun processPendingPayment() {
        val pendingPayment = repository.findByStatus(Status.CREATED)
        logger.info("Processing pending payments... Found ${pendingPayment.size} pending payments")
        coroutineScope {
            pendingPayment.forEach { transaction ->
                launch {
                    transactionProcessor.processWithRetry(transaction)
                }
            }
        }
    }
}
