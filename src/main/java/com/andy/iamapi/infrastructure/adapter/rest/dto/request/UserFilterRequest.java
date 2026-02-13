package com.andy.iamapi.infrastructure.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * DTO para filtros de búsqueda de usuarios.
 *
 * Permite filtrar usuarios por múltiples criterios opcionales.
 * Todos los filtros son opcionales (pueden ser null).
 *
 * Los filtros se aplican con lógica AND (todos deben cumplirse).
 * Los filtros de texto son case-insensitive y usan LIKE (búsqueda parcial).
 */
public record UserFilterRequest(
        @Parameter(description = "Filtrar por email (búsqueda parcial, case-insensitive)", example = "john")
        String email,

        @Parameter(description = "Filtrar por nombre (búsqueda parcial, case-insensitive)", example = "John")
        String firstName,

        @Parameter(description = "Filtrar por apellido (búsqueda parcial, case-insensitive)", example = "Doe")
        String lastName,

        @Parameter(description = "Filtrar por estado habilitado/deshabilitado", example = "true")
        Boolean enabled,

        @Parameter(description = "Filtrar por rol específico", example = "ROLE_ADMIN")
        String role
) {
    /**
     * Verifica si hay algún filtro aplicado.
     *
     * @return true si al menos un filtro no es null
     */
    public boolean hasFilters() {
        return email != null
                        || firstName != null
                        || lastName != null
                        || enabled != null
                        || role != null;
    }
}
