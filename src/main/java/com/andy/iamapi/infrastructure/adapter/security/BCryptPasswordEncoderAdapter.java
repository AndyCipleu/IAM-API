package com.andy.iamapi.infrastructure.adapter.security;

import com.andy.iamapi.domain.port.output.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adapter que implementa el port PasswordEncoder usando BCrypt.
 *
 * BCrypt es un algoritmo de hashing diseñado específicamente para passwords:
 * - Lento a propósito (resistente a fuerza bruta)
 * - Genera salt automáticamente (cada hash es único)
 * - Configurable (factor de trabajo ajustable)
 *
 * El dominio NO conoce BCrypt, solo conoce el port PasswordEncoder.
 * Mañana podríamos cambiar a Argon2 sin tocar el dominio.
 *
 * SOLID aplicado:
 * - Single Responsibility: Solo adapta BCrypt al port del dominio
 * - Dependency Inversion: Implementa abstracción definida por dominio
 * - Open/Closed: Puedes agregar Argon2Adapter sin modificar este
 *
 * @see PasswordEncoder
 * @see BCryptPasswordEncoder
 */
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {
    /**
     * Instancia de BCrypt de Spring Security.
     *
     * Configuración por defecto:
     * - Strength: 10 (2^10 = 1024 rounds)
     * - Tiempo de hash: ~100ms en hardware moderno
     *
     * Puedes ajustar strength para más seguridad (más lento):
     * new BCryptPasswordEncoder(12) → ~400ms
     * new BCryptPasswordEncoder(14) → ~1600ms
     *
     * Recomendación: 10-12 para apps web, 14+ para datos muy sensibles
     */
    private final BCryptPasswordEncoder bcrypt;

    public BCryptPasswordEncoderAdapter() {
        this.bcrypt = new BCryptPasswordEncoder();
    }

    /**
     * Hashea una contraseña en texto plano.
     *
     * BCrypt internamente:
     * 1. Genera un salt aleatorio (16 bytes)
     * 2. Combina password + salt
     * 3. Aplica algoritmo Blowfish 2^strength veces
     * 4. Retorna hash en formato: $2a$10$salt+hash
     *
     * Ejemplo de hash generado:
     * {@code $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy}
     *
     * Características:
     * - Mismo password genera DIFERENTE hash cada vez (salt único)
     * - Irreversible (no puedes obtener password original)
     * - Tiempo constante ~100ms (resistente a timing attacks)
     *
     * @param rawPassword Contraseña en texto plano
     * @return Hash BCrypt (60 caracteres aprox)
     */
    @Override
    public String encode(String rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    /**
     * Verifica si una contraseña coincide con su hash.
     *
     * BCrypt internamente:
     * 1. Extrae el salt del hash almacenado
     * 2. Hashea rawPassword con ese mismo salt
     * 3. Compara el resultado con el hash almacenado
     * 4. Retorna true si coinciden
     *
     * Esto permite que:
     * - Mismo password genere diferentes hashes (salt aleatorio)
     * - Pero matches() siempre retorna true si el password es correcto
     *
     * Ejemplo:
     * {@code String hash1 = encode("password123"); // $2a$10$abc...}
     * {@code String hash2 = encode("password123"); // $2a$10$xyz... (diferente)}
     * {@code matches("password123", hash1); // true}
     * {@code matches("password123", hash2); // true}
     * {@code matches("wrong", hash1);       // false}
     *
     * @param rawPassword Contraseña en texto plano a verificar
     * @param encodedPassword Hash almacenado en la BD
     * @return true si coinciden, false si no
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return bcrypt.matches(rawPassword, encodedPassword);
    }
}
