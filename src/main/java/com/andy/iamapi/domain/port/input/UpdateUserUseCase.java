package com.andy.iamapi.domain.port.input;

import com.andy.iamapi.domain.model.User;

import java.util.UUID;

/**
 * Caso de uso: Actualizar usuario.
 */
public interface UpdateUserUseCase {
    /**
     * Actualiza los datos de un usuario.
     *
     * @param command Comando con datos a actualizar
     * @return Usuario actualizado
     * @throws UserNotFoundException si el usuario no existe
     */
    User execute(UpdateUserCommand command);

    record UpdateUserCommand(
            UUID userId,
            String firstName,
            String lastName,
            String email
    ) {
        public UpdateUserCommand {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (firstName == null || firstName.isBlank()) {
                throw new IllegalArgumentException("First name is required");
            }
            if (lastName == null || lastName.isBlank()) {
                throw new IllegalArgumentException("Last name is required");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email is required");
            }
        }
    }
}
