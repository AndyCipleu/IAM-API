package com.andy.iamapi.domain.port.input;

import java.util.UUID;

/**
 * Caso de uso: Asignar role a un usuario
 */
public interface AssignRoleUseCase {
    void execute(AssignRoleCommand command);

    record AssignRoleCommand(
            String userId,
            String roleName,
            UUID executedBy //Quién ejecuta la accion (auditoría)
    ) {
        public AssignRoleCommand {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (roleName == null || roleName.isBlank()) {
                throw new IllegalArgumentException("Role name cannot be empty");
            }
            if (executedBy == null) {
                throw new IllegalArgumentException("Executor ID cannot be null");
            }
        }
    }
}
