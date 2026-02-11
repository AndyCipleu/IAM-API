package com.andy.iamapi.domain.port.input;

import java.util.UUID;

/**
 * Caso de uso: Asignar rol a usuario.
 */
public interface AssignRoleToUserUseCase {
    /**
     * Asigna un rol a un usuario.
     *
     * @param command Comando con IDs de usuario y rol
     * @throws UserNotFoundException si el usuario no existe
     * @throws RoleNotFoundException si el rol no existe
     */
    void execute(AssignRoleCommand command);

    record AssignRoleCommand(
            UUID userId,
            UUID roleId
    ) {
        public AssignRoleCommand {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (roleId == null) {
                throw new IllegalArgumentException("Role ID is required");
            }
        }
    }
}
