package com.andy.iamapi.infrastructure.adapter.rest.dto.response;


import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de error estandarizadas.
 *
 * Formato consistente de errores para toda la API:
 * {
 *   "timestamp": "2026-02-02T15:30:45",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Email is required",
 *   "path": "/api/auth/register"
 * }
 *
 * Beneficios:
 * - Cliente siempre sabe cómo parsear errores
 * - Debugging más fácil (timestamp, path)
 * - Profesional (no exponer stack traces)
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details //Para múltiples errores de validación(contiene los distintos campos que incumplen la validación)
) {
    //Si no hay details
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    //Si hay details
    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}
