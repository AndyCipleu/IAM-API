package com.andy.iamapi.infrastructure.adapter.security;

import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.output.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del port TokenService usando JWT (JSON Web Tokens).
 *
 * JWT es un estándar para transmitir información de forma segura entre partes.
 *
 * Estructura de un JWT:
 * header.payload.signature
 *
 * Ejemplo:
 * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiZXhwIjoxNjQwOTk1MjAwfQ.signature
 *
 * Header: {"alg":"HS256","typ":"JWT"}
 * Payload: {"sub":"john@example.com","exp":1640995200}
 * Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
 *
 * Ventajas de JWT:
 * - Stateless: no necesitas almacenar sesiones en servidor
 * - Self-contained: contiene toda la info necesaria
 * - Compacto: se envía fácilmente en headers HTTP
 * - Seguro: firmado criptográficamente (no se puede alterar)
 */
@Component
public class JwtTokenService implements TokenService {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    /**
     * Clave secreta para firmar los tokens.
     *
     * Viene de application.yml: jwt.secret
     * IMPORTANTE: En producción usar una clave fuerte y almacenarla en variables de entorno.
     *
     * Requisito: Mínimo 256 bits (32 caracteres) para HMAC-SHA256
     */
    private final SecretKey secretKey;

    /**
     * Tiempo de expiración del access token en milisegundos.
     *
     * Viene de application.yml: jwt.expiration
     * Por defecto: 3600000 ms = 1 hora
     */
    private final long accesTokenExpiration;

    /**
     * Tiempo de expiración del refresh token en milisegundos.
     *
     * Viene de application.yml: jwt.refresh-expiration
     * Por defecto: 604800000 ms = 7 días
     */
    private final long refreshTokenExpiration;

    /**
     * Black list de tokens de Redis
     */
    private final RedisTokenBlacklist tokenBlacklist;

    /**
     * Constructor que inyecta configuración desde application.yml.
     *
     * Value inyecta valores de properties.
     *
     * @param secret Clave secreta para firmar tokens
     * @param accessTokenExpiration Expiración del access token en ms
     * @param refreshTokenExpiration Expiración del refresh token en ms
     */
    public JwtTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpiration,
            RedisTokenBlacklist tokenBlacklist
    ) {
        // Convertir el string secret a SecretKey para HMAC-SHA256
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accesTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.tokenBlacklist = tokenBlacklist;

        log.info("JwtTokenService initialized with access token expiration: {}ms, refresh token expiration: {}ms",
        accessTokenExpiration, refreshTokenExpiration);
    }

    /**
     * Genera un access token para un usuario.
     *
     * Access token:
     * - Corta duración (1 hora por defecto)
     * - Se envía en cada request: Authorization: Bearer {token}
     * - Contiene claims: email, roles
     *
     * Claims incluidos:
     * - sub (subject): email del usuario
     * - roles: lista de roles del usuario
     * - iat (issued at): cuándo se generó
     * - exp (expiration): cuándo expira
     *
     * @param user Usuario autenticado
     * @return JWT access token
     */
    @Override
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accesTokenExpiration);

        //Extraer nombres de roles
        String roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.joining(","));

        String token = Jwts.builder()
                .subject(user.getEmail())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

        log.debug("Generated acces token for user: {}", user.getEmail());

        return token;
    }

    /**
     * Genera un refresh token para renovar la sesión.
     *
     * Refresh token:
     * - Larga duración (7 días por defecto)
     * - Se usa solo en endpoint /auth/refresh
     * - Más simple que access token (solo email)
     *
     * Flujo de uso:
     * 1. Usuario hace login → recibe access token (1h) + refresh token (7d)
     * 2. Access token expira después de 1h
     * 3. Cliente envía refresh token a /auth/refresh
     * 4. Backend genera nuevo access token
     * 5. Cliente continúa usando la app sin re-login
     *
     * @param user Usuario autenticado
     * @return JWT refresh token
     */
    @Override
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        String token = Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

        log.debug("Generated refresh token for user: {}", user.getEmail());

        return token;
    }

    /**
     * Valida un token y extrae el email del usuario.
     *
     * Validaciones que realiza:
     * 1. Firma válida (no fue alterado)
     * 2. No expiró
     * 3. Formato correcto
     *
     * @param token JWT token a validar
     * @return Optional con email si es válido, Optional.empty() si no
     */
    @Override
    public Optional<String> validateTokenAndGetEmail(String token) {
        try {
            //Parsear y validar el token
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();

            log.debug("Token validated successfully for user: {}", email);

            return Optional.of(email);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            return Optional.empty();
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Invalid token signature: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Invalid token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Invalida un token específico.
     *
     * NOTA: Implementación simplificada.
     * En producción, deberías:
     * - Almacenar tokens revocados en Redis o BD
     * - Verificar contra esa lista en validateTokenAndGetEmail()
     *
     * Por ahora, simplemente logueamos.
     * Los tokens expirarán naturalmente.
     *
     * @param token Token a revocar
     */
    @Override
    public void revokeToken(String token) {
        try {
            // Parsear el token para obtener la expiración
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();
            long ttlMillis = expiration.getTime() - now;

            //Agregar a blacklist solo si aún no ha expirado
            if (ttlMillis > 0) {
                Duration ttl = Duration.ofMillis(ttlMillis);
                tokenBlacklist.add(token, ttl);

                log.info("Token revoked and added to Redis blacklist with TTL: {} seconds", ttl.getSeconds());
            } else {
                log.debug("Token already expired, not adding to blacklist");
            }

        } catch (Exception e) {
            log.error("Error revoking token", e);
        }


    }

    /**
     * Verifica si un token ha sido revocado.
     *
     * @param token Token a verificar
     * @return false si no lo contiene, true si lo contiene
     */
    @Override
    public boolean isTokenRevoked(String token) {
        return tokenBlacklist.contains(token);
    }
}
