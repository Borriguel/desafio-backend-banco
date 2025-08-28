package dev.borriguel.desafiobackendbanco.exception

class InsufficientFundsException(message: String = "Insufficient funds") : RuntimeException(message) {
}
