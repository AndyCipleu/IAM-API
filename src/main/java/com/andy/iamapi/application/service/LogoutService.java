package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.port.input.LogoutUseCase;
import com.andy.iamapi.domain.port.output.AuditLogger;
import com.andy.iamapi.domain.port.output.TokenService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Servicio para cerrar sesión (logout).
 *
 * Flujo:
 * 1. Revocar access token (agregar a blacklist)
 * 2. Revocar refresh token (agregar a blacklist)
 * 3. Registrar auditoría
 *
 * Después del logout:
 * - Access token ya no funciona (blacklist)
 * - Refresh token ya no funciona (blacklist)
 * - Usuario debe hacer login de nuevo
 */
@Service
public class LogoutService implements LogoutUseCase {
    private static final Logger log = LoggerFactory.getLogger(LogoutService.class);
    private final TokenService tokenService;
    private final AuditLogger auditLogger;


    public LogoutService (
            TokenService tokenService,
            AuditLogger auditLogger
    ) {
        this.tokenService = tokenService;
        this.auditLogger = auditLogger;
    }

    /**
     * Servicio para cerrar sesión (logout).
     *
     * Flujo:
     * 1. Revocar access token (agregar a blacklist)
     * 2. Revocar refresh token (agregar a blacklist)
     * 3. Registrar auditoría
     *
     * Después del logout:
     * - Access token ya no funciona (blacklist)
     * - Refresh token ya no funciona (blacklist)
     * - Usuario debe hacer login de nuevo
     */
    @Override
    public void execute(LogoutCommand command) {
        log.info("Loggin out user: {}", command.userId());

        //PASO 1: Revocar access token
        tokenService.revokeToken(command.accessToken());

        //PASO 2: Revocar refresh token
        tokenService.revokeToken(command.refreshToken());

        //PASO 3: Auditoría
        auditLogger.logAction(
                command.userId(),
                "USER_LOGOUT",
                "USER:" + command.userId(),
                "SYSTEM"
        );

        log.info("User logged out successfully: {}", command.userId());
    }
}
