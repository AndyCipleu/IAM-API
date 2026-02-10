package com.andy.iamapi.domain.port.input;

import java.util.UUID;

/**
 * Caso de uso: Eliminar usuario.
 */
public interface DeleteUserUseCase {
    /**
     * Elimina un usuario del sistema.
     *
     * @param userId ID del usuario a eliminar
     * @throws UserNotFoundException si el usuario no existe
     */
    void execute(UUID userId);
}
