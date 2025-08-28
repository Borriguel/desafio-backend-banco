package dev.borriguel.desafiobackendbanco.controller

import dev.borriguel.desafiobackendbanco.dto.TransactionDto
import dev.borriguel.desafiobackendbanco.model.Transaction
import dev.borriguel.desafiobackendbanco.service.TransactionService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transactions")
class TransactionController(private val service: TransactionService) {
    @PostMapping
    suspend fun createTransaction(@Valid @RequestBody request: TransactionDto): Transaction {
        return service.createTransaction(request.payerId, request.payeeId, request.value, request.idempotencyKey)
    }

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: Long): Transaction {
        return service.getById(id)
    }

    @GetMapping("/wallet/payer/{payerId}")
    suspend fun getByPayerId(@PathVariable payerId: Long): List<Transaction> {
        return service.getAllByPayerId(payerId)
    }

    @GetMapping("/wallet/payee/{payeeId}")
    suspend fun getByPayeeId(@PathVariable payeeId: Long): List<Transaction> {
        return service.getAllByPayeeId(payeeId)
    }
}
