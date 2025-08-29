package dev.borriguel.desafiobackendbanco.controller

import dev.borriguel.desafiobackendbanco.dto.ClientDto
import dev.borriguel.desafiobackendbanco.dto.ClientWalletDto
import dev.borriguel.desafiobackendbanco.model.Client
import dev.borriguel.desafiobackendbanco.service.ClientService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Clients", description = "Operações relacionadas a clientes")
@RestController
@RequestMapping("clients")
class ClientController(private val service: ClientService) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar cliente", description = "Cria um novo cliente com os dados informados")
    suspend fun create(@Valid @RequestBody clientDto: ClientDto) : Client {
        val client = service.createClient(clientDto.email, clientDto.password, clientDto.name, clientDto.document, clientDto.accountType)
        return client
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna os dados do cliente pelo seu identificador")
    suspend fun getById(@PathVariable id : Long) : Client {
        return service.getById(id)
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "Detalhes do cliente", description = "Retorna os dados do cliente e da sua carteira")
    suspend fun getDetailsById(@PathVariable id : Long) : ClientWalletDto {
        return service.getDetailsById(id)
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna a lista de todos os clientes")
    suspend fun getAll() : List<Client> {
        return service.getAll()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir cliente", description = "Remove um cliente pelo seu identificador")
    suspend fun deleteById(@PathVariable id: Long) {
        service.deleteById(id)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    suspend fun updateById(@PathVariable id: Long, @Valid @RequestBody clientUpdate: ClientDto): Client {
        return service.updateById(id, clientUpdate)
    }

}
