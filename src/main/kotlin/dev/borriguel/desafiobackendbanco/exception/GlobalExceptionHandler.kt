package dev.borriguel.desafiobackendbanco.exception

import dev.borriguel.desafiobackendbanco.dto.ApiErrorDto
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import java.time.OffsetDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException, request: WebRequest): ApiErrorDto {
        val status = HttpStatus.BAD_REQUEST
        val details = e.bindingResult.fieldErrors.map { e -> "${e.field}: ${e.defaultMessage ?: "Invalid value"}"}
        return buildErrorBody(status, "One or more fields are invalid", "Validation failed", request, details)
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: NotFoundException, request: WebRequest): ApiErrorDto {
        val status = HttpStatus.NOT_FOUND
        return buildErrorBody(status, e.message.toString(), "Not found", request)
    }

    @ExceptionHandler(InsufficientFundsException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleInsufficientFundsException(e: InsufficientFundsException, request: WebRequest): ApiErrorDto {
        val status = HttpStatus.UNPROCESSABLE_ENTITY
        return buildErrorBody(status, e.message.toString(), "Insufficient funds", request)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolationException(e: ConstraintViolationException, request: WebRequest): ApiErrorDto {
        val status = HttpStatus.BAD_REQUEST
        val details = e.constraintViolations.map { v -> "${v.propertyPath}: ${v.message}" }
        return buildErrorBody(status, e.message.toString(), "Validation failed", request, details)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: IllegalArgumentException, request: WebRequest): ApiErrorDto {
        val status = HttpStatus.BAD_REQUEST
        return buildErrorBody(status, e.message.toString(), "Invalid request", request)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception, request: WebRequest): ApiErrorDto {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        return buildErrorBody(status, e.message.toString(), "Internal server error", request)
    }

    private fun buildErrorBody(status: HttpStatus, message: String, error: String, request: WebRequest, details: List<String>? = null): ApiErrorDto {
        val path = (request as? ServletWebRequest)?.request?.requestURI ?: ""
        return ApiErrorDto(
            timestamp = OffsetDateTime.now(),
            status = status.value(),
            error = error,
            message = message,
            path = path,
            details = details
        )
    }
}
