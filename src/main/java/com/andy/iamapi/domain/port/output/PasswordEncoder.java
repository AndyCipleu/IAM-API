package com.andy.iamapi.domain.port.output;

/**
 * Port para encoding de contraseñas
 *
 * PRINCIPIO APLICADO:
 * - Dependency Inversion: El dominio no conoce BCrypt, solo necesita hashear
 */
public interface PasswordEncoder {

    /**
     * Hashea una contraseña en texto plano
     * @param rawPassword Contraseña sin hashear
     * @return Contraseña hasheada (irreversible)
     */
    String encode(String rawPassword);

    /**
     * Verifica si una contraseña coincide con su hash
     * @param rawPassword Contraseña en texto plano
     * @param encodedPassword Hash almacenado
     * @return true si coinciden
     */
    boolean matches(String rawPassword, String encodedPassword);
}
