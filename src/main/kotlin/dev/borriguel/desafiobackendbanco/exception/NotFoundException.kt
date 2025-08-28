package dev.borriguel.desafiobackendbanco.exception

class NotFoundException(message: String = "Resource not found") : RuntimeException(message) {
}
