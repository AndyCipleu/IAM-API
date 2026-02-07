package com.andy.iamapi.domain.port.input;

import java.util.UUID;

/**
 * Caso de uso: Cerrar sesión (logout).
 */
public interface LogoutUseCase {
    /**
     * Cierra la sesión del usuario revocando sus tokens.
     *
     * @param command Comando con tokens a revocar
     */
    void execute(LogoutCommand command);

    record LogoutCommand (
            UUID userId,
            String accessToken,
            String refreshToken
    ) {
        public LogoutCommand {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalArgumentException("Access token is required");
            }
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new IllegalArgumentException("Refresh token is required");
            }
        }
    }
}
