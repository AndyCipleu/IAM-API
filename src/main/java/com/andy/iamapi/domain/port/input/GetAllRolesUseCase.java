package com.andy.iamapi.domain.port.input;

import com.andy.iamapi.domain.model.Role;

import java.util.Set;

/**
 * Caso de uso: Obtener todos los roles.
 */
public interface GetAllRolesUseCase {

    /**
     * Obtiene todos los roles del sistema.
     *
     * @return Lista de roles
     */
    Set<Role> execute();
}
