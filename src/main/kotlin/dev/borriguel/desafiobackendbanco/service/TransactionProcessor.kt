package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.exception.InsufficientFundsException
import dev.borriguel.desafiobackendbanco.model.Transaction
import dev.borriguel.desafiobackendbanco.repository.TransactionRepository
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.pow

@Service
class TransactionProcessor(
    private val repository: TransactionRepository,
    private val walletService: WalletService,
    private val authorizationService: AuthorizationService)
{
    val logger: Logger = LoggerFactory.getLogger(TransactionProcessor::class.java)

    @Transactional
    suspend fun processWithRetry(transaction: Transaction, retryCount: Int = 3) {
        logger.info("Processing transaction ${transaction.id}")
        transaction.process()
        repository.save(transaction)
        var attempt = 0
        var success = false
        while (attempt < retryCount && !success) {
            attempt++
            try {
                val isAuthorized = authorizationService.authorize()
                if (isAuthorized) {
                    val payer = walletService.getById(transaction.getPayerId())
                    val payee = walletService.getById(transaction.getPayeeId())
                    payer.transferTo(payee, transaction.getValue())
                    walletService.updateBoth(payer, payee)
                    transaction.authorize()
                    repository.save(transaction)
                    logger.info("Transaction ${transaction.id} processed successfully")
                    success = true
                }
                if (attempt < retryCount) delay(1000)
            } catch (_: InsufficientFundsException) {
                logger.info("transaction ${transaction.id} failed: insufficient funds")
                transaction.fail()
                repository.save(transaction)
                return
            } catch (e: Exception) {
                logger.error("Error processing transaction ${transaction.id}", e)
                if (attempt < retryCount) {
                    val delayTime = 1000L * (2.0.pow(attempt)).toLong()
                    delay(delayTime)
                }
            }
        }
        if (!success) {
            transaction.fail()
            repository.save(transaction)
            logger.info("transaction ${transaction.id} permanently failed after $attempt attempts")
        }
    }
}
