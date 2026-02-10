package com.andy.iamapi.domain.port.input;

import java.util.UUID;

/**
 * Caso de uso: Cambiar contraseña de usuario.
 */
public interface ChangePasswordUseCase {
    /**
     * Cambia la contraseña de un usuario.
     *
     * @param command Comando con contraseñas
     * @throws UserNotFoundException si el usuario no existe
     * @throws InvalidCredentialsException si la contraseña actual es incorrecta
     * @throws InvalidPasswordException si la nueva contraseña no cumple requisitos
     */
    void execute(ChangePasswordCommand command);

    record ChangePasswordCommand(
            UUID userId,
            String currentPassword,
            String newPassword
    ) {
        public ChangePasswordCommand {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new IllegalArgumentException("Current password is required");
            }
            if (newPassword == null || newPassword.isBlank()) {
                throw new IllegalArgumentException("New password is required");
            }
        }
    }
}

