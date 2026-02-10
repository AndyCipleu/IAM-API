package com.andy.iamapi.domain.util;

import com.andy.iamapi.domain.exception.InvalidPasswordException;

/**
 * Utilidad para validar requisitos de seguridad de contraseñas.
 *
 * Requisitos:
 * - Mínimo 8 caracteres
 * - Al menos 1 mayúscula
 * - Al menos 1 minúscula
 * - Al menos 1 dígito
 *
 * Esta clase es reutilizada por:
 * - RegisterUserService (registro)
 * - ChangePasswordService (cambio de contraseña)
 */
public class PasswordValidator {

    // Constantes de configuración
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";

    // Constructor privado (utility class - no debe instanciarse)
    private PasswordValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Valida que una contraseña cumpla los requisitos de seguridad.
     *
     * @param password Contraseña a validar
     * @throws InvalidPasswordException si no cumple requisitos
     */
    public static void validate(String password) {
        validateLength(password);
        validateComplexity(password);
    }

    /**
     * Valida longitud mínima de la contraseña.
     */
    private static void validateLength(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long"
            );
        }
    }

    /**
     * Valida complejidad de la contraseña (mayúscula, minúscula, dígito).
     *
     * Pattern explicado:
     * - ^ = inicio
     * - (?=.*[a-z]) = lookahead que verifica al menos 1 minúscula
     * - (?=.*[A-Z]) = lookahead que verifica al menos 1 mayúscula
     * - (?=.*\\d) = lookahead que verifica al menos 1 dígito
     * - .+ = cualquier carácter, al menos 1 vez
     */
    private static void validateComplexity(String password) {
        if (!password.matches(PASSWORD_PATTERN)) {
            throw new InvalidPasswordException(
                    "Password must contain at least one uppercase letter, " +
                            "one lowercase letter, and one digit"
            );
        }
    }

    /**
     * Obtiene la longitud mínima requerida.
     * Útil para mostrar en mensajes de error o documentación.
     */
    public static int getMinPasswordLength() {
        return MIN_PASSWORD_LENGTH;
    }
}