package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.dto.ClientDto
import dev.borriguel.desafiobackendbanco.model.AccountType
import dev.borriguel.desafiobackendbanco.model.Client
import dev.borriguel.desafiobackendbanco.repository.ClientRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class ClientService(private val repository: ClientRepository, private val walletService: WalletService) {
    suspend fun createClient(email: String, password: String, accountType: AccountType) : Client {
        if (existsByEmail(email)) throw IllegalArgumentException("Client already exists")
        val wallet = walletService.createWallet(accountType)
        val client = Client(
            email = email, password = password,
            walletId = wallet.id ?: throw IllegalStateException("Invalid wallet ID")
        )
        return repository.save(client)
    }

    suspend fun getById(id: Long): Client {
        return repository.findById(id) ?: throw IllegalArgumentException("Client not found")
    }

    suspend fun getByEmail(email: String): Client? {
        return repository.findByEmail(email) ?: throw IllegalArgumentException("Client not found")
    }

    private suspend fun existsByEmail(email: String): Boolean {
        return repository.existsByEmail(email)
    }

    suspend fun deleteById(id: Long) {
        getById(id)
        repository.deleteById(id)
    }

    suspend fun getAll(): List<Client> {
        return repository.findAll().toList()
    }

    suspend fun updateById(id: Long, clientUpdate: ClientDto): Client {
        val existingClient = getById(id)
        existingClient.changeEmail(clientUpdate.email)
        existingClient.changePassword(clientUpdate.password)
        return repository.save(existingClient)
    }

}
