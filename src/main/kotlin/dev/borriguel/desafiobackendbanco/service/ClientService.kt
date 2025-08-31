package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.dto.ClientDto
import dev.borriguel.desafiobackendbanco.dto.ClientWalletDto
import dev.borriguel.desafiobackendbanco.exception.NotFoundException
import dev.borriguel.desafiobackendbanco.model.AccountType
import dev.borriguel.desafiobackendbanco.model.Client
import dev.borriguel.desafiobackendbanco.repository.ClientRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClientService(private val repository: ClientRepository, private val walletService: WalletService) {

    @Transactional
    suspend fun createClient(email: String, password: String, name: String, document: String, accountType: AccountType) : Client {
        if (existsByEmail(email)) throw IllegalArgumentException("Client already exists")
        if (existsByDocument(document)) throw IllegalArgumentException("Document already exists")
        val wallet = walletService.createWallet(accountType)
        val client = Client(
            email = email,
            password = password,
            name = name,
            document = document,
            walletId = wallet.id ?: throw IllegalStateException("Invalid wallet ID")
        )
        return repository.save(client)
    }

    suspend fun getById(id: Long): Client {
        return repository.findById(id) ?: throw NotFoundException("Client not found")
    }

    private suspend fun existsByEmail(email: String): Boolean {
        return repository.existsByEmail(email)
    }

    private suspend fun existsByDocument(document: String): Boolean {
        return repository.existsByDocument(document)
    }

    suspend fun deleteById(id: Long) {
        getById(id)
        repository.deleteById(id)
    }

    suspend fun getAll(): List<Client> {
        return repository.findAll().toList()
    }

    @Transactional
    suspend fun updateById(id: Long, clientUpdate: ClientDto): Client {
        val existingClient = getById(id)
        if (repository.existsByEmail(clientUpdate.email) && existingClient.getEmail() != clientUpdate.email)
            throw IllegalArgumentException("Email already exists")
        if (repository.existsByDocument(clientUpdate.document) && existingClient.getDocument() != clientUpdate.document)
            throw IllegalArgumentException("Document already exists")
        existingClient.changeEmail(clientUpdate.email)
        existingClient.changeName(clientUpdate.name)
        existingClient.changeDocument(clientUpdate.document)
        existingClient.changePassword(clientUpdate.password)
        return repository.save(existingClient)
    }

    suspend fun getDetailsById(id: Long): ClientWalletDto {
        val client = getById(id)
        val wallet = walletService.getById(client.getWallet())
        val clientDetails = ClientWalletDto(
            id,
            client.getName(),
            client.getDocument(),
            wallet
        )
        return clientDetails
    }

}
