package com.andy.iamapi.infrastructure.adapter.rest.dto.request;

import com.andy.iamapi.domain.port.input.ChangePasswordUseCase.ChangePasswordCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO para solicitud de cambio de contraseña.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword
) {

    /**
     * Convierte el DTO a Command del dominio.
     *
     * @param userId ID del usuario (viene del SecurityContext)
     * @return ChangePasswordCommand
     */
    public ChangePasswordCommand toCommand(UUID userId) {
        return new ChangePasswordCommand(
                userId,
                this.currentPassword(),
                this.newPassword()
        );
    }
}
