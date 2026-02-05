package com.andy.iamapi.infrastructure.adapter.rest.exception;

import com.andy.iamapi.domain.exception.*;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para toda la API REST.
 *
 * RestControllerAdvice intercepta excepciones lanzadas por cualquier controller
 * y las convierte en respuestas JSON estandarizadas.
 *
 * Ventajas:
 * - Respuestas consistentes en toda la API
 * - Controllers más limpios (no manejan excepciones)
 * - Un solo lugar para logging de errores
 * - Fácil agregar nuevas excepciones
 *
 * Cada método con ExceptionHandler captura un tipo específico de excepción
 * y retorna un ResponseEntity con ErrorResponse.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de validación de Bean Validation.
     *
     * Se lanza cuando los DTOs con anotaciones de validación
     * (NotBlank, Email, Size, etc) fallan.
     *
     * Ejemplo:
     * RegisterUserRequest con email vacío → MethodArgumentNotValidException
     *
     * Retorna 400 Bad Request con todos los errores de validación.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                MethodArgumentNotValidException ex,
                HttpServletRequest request) {

            // Extraer todos los errores de validación
            List<String> details = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());

            log.warn("Validation failed for request to {}: {}", request.getRequestURI(), details);

            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    "Validation failed",
                    request.getRequestURI(),
                    details
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);

        }

    /**
     * Maneja UserAlreadyExistsException.
     *
     * Se lanza cuando se intenta registrar un email que ya existe.
     *
     * Retorna 409 Conflict.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        log.warn("User already exists: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Maneja InvalidPasswordException.
     *
     * Se lanza cuando la contraseña no cumple requisitos de seguridad.
     *
     * Retorna 400 Bad Request.
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(
            InvalidPasswordException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid password attempt for request to {}", request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Maneja UserNotFoundException.
     *
     * Se lanza cuando se busca un usuario que no existe.
     *
     * Retorna 404 Not Found.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {

        log.warn("User not found {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Maneja InvalidCredentialsException.
     *
     * Se lanza cuando email o password son incorrectos en el login.
     *
     * Retorna 401 Unauthorized.
     *
     * IMPORTANTE: El mensaje es genérico por seguridad.
     * No revelamos si el email existe o si la password es incorrecta.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {

        log.warn("Invalid credentials attempt from {}", request.getRemoteAddr());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * Maneja AccountLockedException.
     *
     * Se lanza cuando se intenta acceder a una cuenta bloqueada.
     *
     * Retorna 403 Forbidden.
     */
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(
            AccountLockedException ex,
            HttpServletRequest request
    ) {

        log.warn("Account locked: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Maneja RoleNotFoundException.
     *
     * Se lanza cuando se intenta asignar un rol que no existe.
     *
     * Retorna 404 Not Found.
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(
            RoleNotFoundException ex,
            HttpServletRequest request
    ) {

        log.warn("Role not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Maneja IllegalArgumentException.
     *
     * Se lanza por validaciones en los Commands (compact constructor).
     *
     * Retorna 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Invalid argument for request to {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Maneja todas las excepciones NO capturadas por handlers específicos.
     *
     * Este es el catch-all para excepciones inesperadas.
     *
     * Retorna 500 Internal Server Error.
     *
     * IMPORTANTE:
     * - En desarrollo: Logea stack trace completo
     * - En producción: Mensaje genérico (no exponer detalles internos)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // Log completo del error (incluye stack trace)
        log.error("Unexpected error processing request to {}", request.getRequestURI(), ex);

        // Mensaje genérico para el cliente (seguridad)
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
