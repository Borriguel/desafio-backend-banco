package dev.borriguel.desafiobackendbanco.controller

import dev.borriguel.desafiobackendbanco.dto.ClientDto
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

@RestController
@RequestMapping("clients")
class ClientController(private val service: ClientService) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@Valid @RequestBody clientDto: ClientDto) : Client {
        val client = service.createClient(clientDto.email, clientDto.password, clientDto.name, clientDto.document, clientDto.accountType)
        return client
    }

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id : Long) : Client {
        return service.getById(id)
    }

    @GetMapping
    suspend fun getAll() : List<Client> {
        return service.getAll()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteById(@PathVariable id: Long) {
        service.deleteById(id)
    }

    @PutMapping("/{id}")
    suspend fun updateById(@PathVariable id: Long, @Valid @RequestBody clientUpdate: ClientDto): Client {
        return service.updateById(id, clientUpdate)
    }

}
