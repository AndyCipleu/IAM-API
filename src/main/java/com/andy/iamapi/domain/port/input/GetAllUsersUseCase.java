package com.andy.iamapi.domain.port.input;

import com.andy.iamapi.domain.model.User;

import java.util.List;

/**
 * Caso de uso: Obtener todos los usuarios.
 */
public interface GetAllUsersUseCase {
    /**
     * Obtiene todos los usuarios del sistema.
     *
     * @return Lista de usuarios
     */
    List<User> execute();
}
