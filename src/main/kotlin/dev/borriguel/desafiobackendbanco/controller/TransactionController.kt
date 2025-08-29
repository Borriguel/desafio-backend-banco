package dev.borriguel.desafiobackendbanco.controller

import dev.borriguel.desafiobackendbanco.dto.TransactionDto
import dev.borriguel.desafiobackendbanco.model.Transaction
import dev.borriguel.desafiobackendbanco.service.TransactionService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Transactions", description = "Operações de transações entre carteiras")
@RestController
@RequestMapping("/transactions")
class TransactionController(private val service: TransactionService) {
    @PostMapping
    @Operation(summary = "Criar transação", description = "Cria uma transação entre pagador e recebedor")
    suspend fun createTransaction(@Valid @RequestBody request: TransactionDto): Transaction {
        return service.createTransaction(request.payerId, request.payeeId, request.value, request.idempotencyKey)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID", description = "Retorna a transação pelo seu identificador")
    suspend fun getById(@PathVariable id: Long): Transaction {
        return service.getById(id)
    }

    @GetMapping("/wallet/payer/{payerId}")
    @Operation(summary = "Listar por pagador", description = "Lista as transações onde o ID informado é o pagador")
    suspend fun getByPayerId(@PathVariable payerId: Long): List<Transaction> {
        return service.getAllByPayerId(payerId)
    }

    @GetMapping("/wallet/payee/{payeeId}")
    @Operation(summary = "Listar por recebedor", description = "Lista as transações onde o ID informado é o recebedor")
    suspend fun getByPayeeId(@PathVariable payeeId: Long): List<Transaction> {
        return service.getAllByPayeeId(payeeId)
    }
}
