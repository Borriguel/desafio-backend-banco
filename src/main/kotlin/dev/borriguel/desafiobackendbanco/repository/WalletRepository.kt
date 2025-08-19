package dev.borriguel.desafiobackendbanco.repository

import dev.borriguel.desafiobackendbanco.model.Wallet
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface WalletRepository: CoroutineCrudRepository<Wallet, Long>
