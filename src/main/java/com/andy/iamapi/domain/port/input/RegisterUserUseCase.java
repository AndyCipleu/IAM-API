package com.andy.iamapi.domain.port.input;

import com.andy.iamapi.domain.model.User;

/**
 * Caso de uso: Registrar un nuevo usuario
 *
 * PRINCIPIO APLICADO:
 * - Single Responsibility: Solo se encarga del registro
 * - Interface Segregation: Interfaz específica para este caso de uso
 */
public interface RegisterUserUseCase {
    /**
     * Registra un nuevo usuario en el sistema
     *
     * @param command Comando con datos del registro
     * @return Usuario creado con roles por defecto
     * @throws UserAlreadyExistsException si el email ya está registrado
     * @throws InvalidPasswordException si la contraseña no cumple requisitos
     */
    User execute(RegisterUserCommand command);

    record RegisterUserCommand(
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        public RegisterUserCommand {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email cannot be empty");
            }
            if (password == null || password.length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters");
            }
            if (firstName == null || firstName.isBlank()) {
                throw new IllegalArgumentException("First name cannot be empty");
            }
            if (lastName == null || lastName.isBlank()) {
                throw new IllegalArgumentException("Last name cannot be empty");
            }
        }
    }

}
