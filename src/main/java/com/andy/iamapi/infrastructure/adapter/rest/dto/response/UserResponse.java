package com.andy.iamapi.infrastructure.adapter.rest.dto.response;


import com.andy.iamapi.domain.model.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO para respuesta con datos de usuario.
 *
 * Este DTO define QUÉ datos expone la API sobre un usuario.
 *
 * Campos que NO incluimos (seguridad):
 * - password (nunca exponerla, ni hasheada)
 * - accountNonLocked (info interna de seguridad)
 *
 * Campos que SÍ incluimos:
 * - Datos públicos (id, email, nombre)
 * - Roles (para que el frontend sepa qué mostrar)
 */
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Set<String> roles, //Sólo nombres de los roles, no objetos completos
        LocalDateTime createdAt
) {
    /**
     * Crea un UserResponse desde una entidad de dominio.
     *
     * Este método es un mapper manual (alternativa a MapStruct).
     * Controla exactamente qué exponer.
     *
     * @param user Entidad de dominio
     * @return DTO para la API
     */
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
}
