package dev.borriguel.desafiobackendbanco.service

import dev.borriguel.desafiobackendbanco.dto.ClientDto
import dev.borriguel.desafiobackendbanco.exception.NotFoundException
import dev.borriguel.desafiobackendbanco.model.AccountType
import dev.borriguel.desafiobackendbanco.model.Client
import dev.borriguel.desafiobackendbanco.model.Wallet
import dev.borriguel.desafiobackendbanco.repository.ClientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(MockitoExtension::class)
class ClientServiceTest {
    @Mock
    private lateinit var repository: ClientRepository
    @Mock
    private lateinit var walletService: WalletService
    @InjectMocks
    private lateinit var service: ClientService

    private val email = "test@email.com"
    private val name = "test"
    private val document = "12345678900"
    private val password = "test123"
    private val accountType = AccountType.COMMON

    @Test
    fun `createClient should return a new client`() = runTest {
        val wallet = Wallet(1L, BigDecimal.ZERO, AccountType.COMMON)
        val client = Client(id = 1L, name = name, email = email, document = document, password = password, walletId = 1L)
        whenever(repository.existsByEmail(email)).thenReturn(false)
        whenever(repository.existsByDocument(document)).thenReturn(false)
        whenever(walletService.createWallet(accountType)).thenReturn(wallet)
        whenever(repository.save(any<Client>())).thenReturn(client)
        val result = service.createClient(email, password, name, document, accountType)
        assertEquals(client, result)
        verify(repository).existsByEmail(email)
        verify(repository).existsByDocument(document)
        verify(walletService).createWallet(accountType)
        verify(repository).save(any<Client>())
    }

    @Test
    fun `createClient should throw IllegalArgumentException when email already exists`()= runTest {
        whenever(repository.existsByEmail(any())).thenReturn(true)
        val exception = assertThrows<IllegalArgumentException> { service.createClient(email, password, name, document, accountType) }
        assertEquals("Client already exists", exception.message)
        verify(repository).existsByEmail(email)
        verify(repository, never()).save(any<Client>())
        verify(walletService, never()).createWallet(accountType)
        verify(repository, never()).existsByDocument(document)
    }

    @Test
    fun `createClient should throw IllegalArgumentException when document already exists`()= runTest {
        whenever(repository.existsByEmail(email)).thenReturn(false)
        whenever(repository.existsByDocument(document)).thenReturn(true)
        val exception = assertThrows<IllegalArgumentException> {
            service.createClient(email, password, name, document, accountType)
        }
        assertEquals("Document already exists", exception.message)
        verify(repository).existsByEmail(email)
        verify(repository).existsByDocument(document)
        verify(repository, never()).save(any<Client>())
        verify(walletService, never()).createWallet(accountType)
    }

    @Test
    fun `findById should return client when found`() = runTest {
        val id = 1L
        val client = Client(id = id, name = name, email = email, document = document, password = password, walletId = 1L)
        whenever(repository.findById(id)).thenReturn(client)
        val result = service.getById(id)
        assertEquals(client, result)
    }

    @Test
    fun `findById should throw NotFoundException when client not found`() = runTest {
        val id = 1L
        whenever(repository.findById(id)).thenReturn(null)
        val exception = assertThrows<NotFoundException> { service.getById(id) }
        assertEquals("Client not found", exception.message)
        verify(repository).findById(id)
    }

    @Test
    fun `deleteById should delete client successfully`() = runTest {
        val id = 1L
        val client = Client(id = id, name = name, email = email, document = document, password = password, walletId = 1L)
        whenever(repository.findById(id)).thenReturn(client)
        service.deleteById(id)
        verify(repository).findById(id)
        verify(repository).deleteById(id)
    }

    @Test
    fun `deleteById should throw NotFoundException when client not found`() = runTest {
        val id = 1L
        whenever(repository.findById(id)).thenReturn(null)
        val exception = assertThrows<NotFoundException> { service.deleteById(id) }
        assertEquals("Client not found", exception.message)
    }

    @Test
    fun `getAll should return all clients`() = runTest {
        val clients = listOf(Client(id = 1L, name = name, email = email, document = document, password = password, walletId = 1L))
        whenever(repository.findAll()).thenReturn(flowOf( *clients.toTypedArray()))
        val result = service.getAll()
        assertEquals(clients, result)
        verify(repository).findAll()
    }

    @Test
    fun `UpdateById should update client successfully`() = runTest {
        val existingClient = Client(id = 1L, name = name, email = email, document = document, password = password, walletId = 1L)
        val clientUpdate = ClientDto("updated@email.com", "updated", "updated", "12345678900", AccountType.COMMON)
        val updatedClient = Client(1L, clientUpdate.email, clientUpdate.name, clientUpdate.document, clientUpdate.password, 1L)
        whenever(repository.findById(1L)).thenReturn(existingClient)
        whenever(repository.existsByEmail(clientUpdate.email)).thenReturn(false)
        whenever(repository.existsByDocument(clientUpdate.document)).thenReturn(false)
        whenever(repository.save(any<Client>())).thenReturn(updatedClient)
        val result = service.updateById(1L, clientUpdate)
        assertEquals(updatedClient, result)
        verify(repository).findById(1L)
        verify(repository).existsByEmail(clientUpdate.email)
        verify(repository).existsByDocument(clientUpdate.document)
        verify(repository).save(any<Client>())
    }

    @Test
    fun `UpdateById should throw IllegalArgumentException when email already exists`() = runTest {
        val clientId = 1L
        val clientUpdate = ClientDto(
            email = "new@email.com",
            password = "newpassword",
            name = "Updated User",
            document = "98765432100",
            accountType = AccountType.COMMON
        )
        val existingClient = Client(clientId, email, name, document, password, 1L)
        whenever(repository.findById(clientId)).thenReturn(existingClient)
        whenever(repository.existsByEmail(clientUpdate.email)).thenReturn(true)
        val exception = assertThrows<IllegalArgumentException> {
            service.updateById(clientId, clientUpdate)
        }
        assertEquals("Email already exists", exception.message)
        verify(repository).findById(clientId)
        verify(repository).existsByEmail(clientUpdate.email)
        verify(repository, never()).save(any<Client>())
    }

    @Test
    fun `UpdateById should throw IllegalArgumentException when document already exists`() = runTest {
        val clientId = 1L
        val clientUpdate = ClientDto(
            email = "new@email.com",
            password = "newpassword",
            name = "Updated User",
            document = "98765432100",
            accountType = AccountType.COMMON
        )
        val existingClient = Client(clientId, email, name, document, password, 1L)
        whenever(repository.findById(clientId)).thenReturn(existingClient)
        whenever(repository.existsByEmail(clientUpdate.email)).thenReturn(false)
        whenever(repository.existsByDocument(clientUpdate.document)).thenReturn(true)
        val exception = assertThrows<IllegalArgumentException> {
            service.updateById(clientId, clientUpdate)
        }
        assertEquals("Document already exists", exception.message)
        verify(repository).findById(clientId)
        verify(repository).existsByEmail(clientUpdate.email)
        verify(repository).existsByDocument(clientUpdate.document)
        verify(repository, never()).save(any<Client>())
    }

    @Test
    fun `getDetailsById should return client wallet details`() = runTest {
        val clientId = 1L
        val client = Client(id = clientId, name = name, email = email, document = document, password = password, walletId = 1L)
        val wallet = Wallet(id = 1L, balance = BigDecimal("100.00"), type = AccountType.COMMON)
        whenever(repository.findById(clientId)).thenReturn(client)
        whenever(walletService.getById(1L)).thenReturn(wallet)
        val result = service.getDetailsById(clientId)
        assertEquals(clientId, result.id)
        assertEquals(name, result.name)
        assertEquals(document, result.document)
        assertEquals(wallet, result.wallet)
        verify(repository).findById(clientId)
        verify(walletService).getById(1L)
    }
}
