package com.andy.iamapi.infrastructure.adapter.rest.dto.request;

import com.andy.iamapi.domain.port.input.UpdateUserUseCase.UpdateUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * DTO para solicitud de actualización de usuario.
 */
public record UpdateUserRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
    /**
     * Convierte el DTO a Command del dominio.
     *
     * @param userId ID del usuario a actualizar (viene del path)
     * @return UpdateUserCommand
     */
    public UpdateUserCommand toCommand(UUID userId) {
        return new UpdateUserCommand(
                userId,
                this.firstName(),
                this.lastName(),
                this.email()
        );
    }
}
