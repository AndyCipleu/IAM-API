package com.andy.iamapi.domain.port.input;

import com.andy.iamapi.domain.model.User;

import java.util.UUID;

/**
 * Caso de uso: Obtener usuario por ID.
 */
public interface GetUserByIdUseCase {
    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID del usuario
     * @return Usuario
     * @throws UserNotFoundException si el usuario no existe
     */
    User execute(UUID userId);
}
