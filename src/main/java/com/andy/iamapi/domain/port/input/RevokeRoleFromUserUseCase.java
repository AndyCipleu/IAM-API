package com.andy.iamapi.domain.port.input;

import java.util.UUID;

/**
 * Caso de uso: Revocar rol de usuario.
 */
public interface RevokeRoleFromUserUseCase {
    /**
     * Revoca un rol de un usuario.
     *
     * @param command Comando con IDs de usuario y rol
     * @throws UserNotFoundException si el usuario no existe
     * @throws RoleNotFoundException si el rol no existe
     */
    void execute(RevokeRoleCommand command);

    record RevokeRoleCommand(
            UUID userId,
            UUID roleId
    ) {
        public RevokeRoleCommand {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (roleId == null) {
                throw new IllegalArgumentException("Role ID is required");
            }
        }
    }
}
