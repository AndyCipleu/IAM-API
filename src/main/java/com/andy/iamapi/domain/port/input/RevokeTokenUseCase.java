package com.andy.iamapi.domain.port.input;

/**
 * Caso de uso: Revocar un token (logout)
 */
public interface RevokeTokenUseCase {
    void execute(RevokeTokenCommand command);

    record RevokeTokenCommand(
            String token
    ) {
        public RevokeTokenCommand {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Token cannot be empty");
            }
        }
    }
}
