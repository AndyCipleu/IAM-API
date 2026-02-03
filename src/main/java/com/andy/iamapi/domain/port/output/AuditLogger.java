package com.andy.iamapi.domain.port.output;

import java.util.UUID;

/**
 * Port para auditoría de acciones (quién hizo qué)
 */
public interface AuditLogger {
    /**
     * Registra una acción de un usuario
     * @param userId ID del usuario que ejecuta la acción
     * @param action Acción realizada (ej: "USER_REGISTERED", "ROLE_ASSIGNED")
     * @param resource Recurso afectado (ej: "USER:john@example.com")
     * @param ipAddress IP desde donde se ejecutó
     */
    void logAction(UUID userId, String action, String resource, String ipAddress);

    /**
     * Registra un intento de acceso fallido
     * @param email Email del intento
     * @param reason Razón del fallo (ej: "INVALID_PASSWORD")
     * @param ipAddress IP del intento
     */
    void logFailedAccess(String email, String reason, String ipAddress);
}
