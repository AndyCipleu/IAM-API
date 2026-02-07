package com.andy.iamapi.domain.port.input;

import com.andy.iamapi.domain.exception.InvalidCredentialsException;

/**
 * Caso de uso: Refrescar access token usando refresh token.
 */
public interface RefreshTokenUseCase {
    /**
     * Genera un nuevo access token usando un refresh token válido.
     *
     * @param command Comando con refresh token
     * @return Nuevo access token
     * @throws InvalidCredentialsException si el refresh token es inválido
     */
    RefreshTokenResult execute(RefreshTokenCommand command);

    record RefreshTokenCommand(
            String refreshToken
    ) {
        public RefreshTokenCommand {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new InvalidCredentialsException("Refresh token is required");
            }
        }
    }

    record RefreshTokenResult(
            String accessToken,
            long expiresIn
    ) {}
}
