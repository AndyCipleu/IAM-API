package com.andy.iamapi.infrastructure.adapter.security;

import com.andy.iamapi.domain.port.output.AuditLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Adapter que implementa el port AuditLogger usando SLF4J.
 *
 * Esta es una implementación SIMPLE para desarrollo.
 * En producción, considera:
 * - Persistir auditoría en BD (tabla audit_logs)
 * - Enviar a servicio centralizado (ELK, Splunk, Datadog)
 * - Usar Spring Data Envers para auditoría automática
 *
 * Ventajas de esta implementación simple:
 * - Cero configuración adicional
 * - Logs en consola y archivos (según logback.xml)
 * - Suficiente para desarrollo y debugging
 *
 * Desventajas:
 * - No queryable fácilmente
 * - Logs rotan y se pierden
 * - No apto para cumplimiento (SOC2, ISO 27001)
 *
 * @see AuditLogger
 */
@Component
public class LoggerAuditAdapter implements AuditLogger {
    /**
     * Logger específico para auditoría.
     *
     * Usa un logger separado para poder:
     * - Configurar nivel diferente (siempre INFO en producción)
     * - Enviar a archivo separado (audit.log)
     * - Filtrar fácilmente en herramientas de análisis
     *
     * En logback.xml podrías configurar:
     * {@code
     * <logger name="AUDIT" level="INFO" additivity="false">
     *   <appender-ref ref="AUDIT_FILE" />
     * </logger>
     * }
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Formateador de fechas para logs legibles.
     *
     * Formato ISO 8601: 2026-02-02T15:30:45
     * Estándar internacional, parseable por herramientas
     */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Registra una acción exitosa de un usuario.
     *
     * Formato del log:
     * {@code [AUDIT] timestamp | userId | action | resource | ipAddress}
     *
     * Ejemplo:
     * {@code [AUDIT] 2026-02-02T15:30:45 | uuid-123 | USER_REGISTERED | USER:john@test.com | 192.168.1.1}
     *
     * Este formato es:
     * - Parseable (separado por |)
     * - Legible para humanos
     * - Contiene toda la info necesaria
     *
     * @param userId ID del usuario que ejecutó la acción
     * @param action Acción realizada (USER_REGISTERED, ROLE_ASSIGNED, etc)
     * @param resource Recurso afectado (USER:email, ROLE:name, etc)
     * @param ipAddress IP desde donde se ejecutó la acción
     */
    @Override
    public void logAction(UUID userId, String action, String resource, String ipAddress) {
        String timestamp = LocalDateTime.now().format(FORMATTER);

        // Formato estructurado para fácil parsing
        String logMessage = String.format(
                "[AUDIT] %s | userId=%s | action=%s | resource=%s | ip=%s",
                timestamp,
                userId,
                action,
                resource,
                ipAddress
        );

        AUDIT.info(logMessage);
    }

    /**
     * Registra un intento de acceso fallido.
     *
     * Importante para detectar:
     * - Ataques de fuerza bruta
     * - Intentos de acceso no autorizados
     * - Cuentas comprometidas
     *
     * Formato del log:
     * {@code [AUDIT_FAIL] timestamp | email | reason | ipAddress}
     *
     * Ejemplo:
     * {@code [AUDIT_FAIL] 2026-02-02T15:30:45 | john@test.com | INVALID_PASSWORD | 192.168.1.1}
     *
     * Con herramientas de análisis puedes alertar:
     * - Más de 5 intentos fallidos en 10 minutos → bloquear IP
     * - Mismo usuario desde 10 IPs diferentes → cuenta comprometida
     *
     * @param email Email del intento fallido
     * @param reason Razón del fallo (INVALID_PASSWORD, ACCOUNT_LOCKED, USER_NOT_FOUND)
     * @param ipAddress IP del intento
     */
    @Override
    public void logFailedAccess(String email, String reason, String ipAddress) {
        String timestamp = LocalDateTime.now().format(FORMATTER);

        String logMessage = String.format(
                "[AUDIT_FAIL %s | email=%s | reason=%s | ip=%s ]",
                timestamp,
                email,
                reason,
                ipAddress
        );

        AUDIT.warn(logMessage);
    }

}
