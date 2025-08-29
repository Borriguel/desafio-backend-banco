package dev.borriguel.desafiobackendbanco.controller

import dev.borriguel.desafiobackendbanco.dto.WalletOperationsDto
import dev.borriguel.desafiobackendbanco.model.Wallet
import dev.borriguel.desafiobackendbanco.service.WalletService
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Wallets", description = "Operações com carteiras")
@RestController
@RequestMapping("/wallets")
data class WalletController(private val service: WalletService) {
    @GetMapping("{id}")
    @Operation(summary = "Buscar carteira por ID", description = "Retorna a carteira pelo seu identificador")
    suspend fun getById(@PathVariable id: Long): Wallet {
        return service.getById(id)
    }

    @PostMapping("{id}/deposit")
    @Operation(summary = "Depositar em carteira", description = "Realiza um depósito na carteira informada")
    suspend fun depositById(@PathVariable id: Long, @Valid @RequestBody request: WalletOperationsDto): Wallet {
        return service.depositById(id, request.amount)
    }

    @PostMapping("{id}/withdraw")
    @Operation(summary = "Sacar da carteira", description = "Realiza um saque da carteira informada")
    suspend fun withdrawById(@PathVariable id: Long, @Valid @RequestBody request: WalletOperationsDto): Wallet {
        return service.withdrawById(id, request.amount)
    }
}
