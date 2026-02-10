package com.andy.iamapi.infrastructure.adapter.rest.dto.response;
/**
 * DTO para respuesta de refresh token.
 *
 * Contiene el nuevo access token generado.
 */
public record RefreshTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    /**
     * Constructor con valores por defecto.
     */
    public RefreshTokenResponse(String accessToken, long expiresIn) {
        this(accessToken,"Bearer", expiresIn);
    }
}
