package com.andy.iamapi.domain.port.input;

import com.andy.iamapi.domain.model.User;

import java.util.UUID;

/**
 * Caso de uso: Obtener usuario actual (me).
 */
public interface GetCurrentUserUseCase {
    /**
     * Obtiene el usuario autenticado actualmente.
     *
     * @param userId ID del usuario autenticado
     * @return Usuario
     * @throws UserNotFoundException si el usuario no existe
     */
    User execute(UUID userId);
}
