package com.andy.iamapi.domain.port.output;

import com.andy.iamapi.domain.model.User;

import java.util.Optional;

/**
 * Port para generación y validación de tokens JWT
 */
public interface TokenService {

    /**
     * Genera un access token para un usuario
     * @param user Usuario autenticado
     * @return JWT token válido por 1 hora (configurable)
     */
    String generateAccesToken(User user);

    /**
     * Genera un refresh token para renovar sesión
     * @param user Usuario autenticado
     * @return Refresh token válido por 7 días (configurable)
     */
    String generateRefreshToken(User user);

    /**
     * Valida un token y extrae el email del usuario
     * @param token JWT token
     * @return Optional con el email si el token es válido
     */
    Optional<String> validateTokenAndGetEmail(String token);

    /**
     * Invalida un token específico (para logout)
     * @param token Token a invalidar
     */
    void revokeToken(String token);

    /**
     * Verifica si un token ha sido revocado
     * @param token Token a verificar
     * @return true si está revocado
     */
    boolean isTokenRevoked(String token);
}
