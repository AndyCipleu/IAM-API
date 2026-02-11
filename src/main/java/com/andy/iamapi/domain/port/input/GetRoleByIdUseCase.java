package com.andy.iamapi.domain.port.input;

import com.andy.iamapi.domain.model.Role;

import java.util.UUID;

/**
 * Caso de uso: Obtener rol por ID.
 */
public interface GetRoleByIdUseCase {
    /**
     * Obtiene un rol por su ID.
     *
     * @param roleId ID del rol
     * @return Rol
     * @throws RoleNotFoundException si el rol no existe
     */
    Role execute(UUID roleId);
}
