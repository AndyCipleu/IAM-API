package com.andy.iamapi.infrastructure.adapter.rest.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitud de registro de usuario.
 *
 * Este DTO define QUÉ datos acepta la API para registro.
 * Separado del modelo de dominio para:
 * - Controlar exactamente qué datos acepta la API
 * - Validar en la capa REST (antes de llegar al dominio)
 * - Evolucionar API sin afectar dominio
 *
 * Anotaciones de validación (Bean Validation):
 * - Se ejecutan automáticamente cuando Spring recibe la request
 * - Si fallan, Spring retorna 400 Bad Request automáticamente
 * - No llega al controller si la validación falla
 */
public record RegisterUserRequest (

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Password is required")
    @Size(message = "Password must be at least 8 characters")
    String password,

    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName
) {
    // Los records son perfectos para DTOs:
    // - Inmutables
    // - Constructor, equals, hashCode, toString automáticos
    // - Sintaxis concisa
}
