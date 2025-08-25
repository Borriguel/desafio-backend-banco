package dev.borriguel.desafiobackendbanco.controller

import dev.borriguel.desafiobackendbanco.dto.WalletOperationsDto
import dev.borriguel.desafiobackendbanco.model.Wallet
import dev.borriguel.desafiobackendbanco.service.WalletService
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/wallets")
data class WalletController(private val service: WalletService) {
    @GetMapping("{id}")
    suspend fun getById(@PathVariable id: Long): Wallet {
        return service.getById(id)
    }

    @PostMapping("{id}/deposit")
    suspend fun depositById(@PathVariable id: Long, @Valid @RequestBody request: WalletOperationsDto): Wallet {
        return service.depositById(id, request.amount)
    }

    @PostMapping("{id}/withdraw")
    suspend fun withdrawById(@PathVariable id: Long, @Valid @RequestBody request: WalletOperationsDto): Wallet {
        return service.withdrawById(id, request.amount)
    }
}
