package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.AccountLockedException;
import com.andy.iamapi.domain.exception.InvalidCredentialsException;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.AuthenticateUserUseCase;
import com.andy.iamapi.domain.port.output.AuditLogger;
import com.andy.iamapi.domain.port.output.PasswordEncoder;
import com.andy.iamapi.domain.port.output.TokenService;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación para autenticar usuarios.
 *
 * Responsabilidades:
 * - Validar credenciales (email + password)
 * - Verificar estado de la cuenta (enabled, locked)
 * - Generar tokens JWT (access + refresh)
 * - Registrar auditoría de login
 *
 * Flujo de autenticación:
 * 1. Buscar usuario por email
 * 2. Verificar que existe
 * 3. Verificar password con BCrypt
 * 4. Verificar cuenta habilitada
 * 5. Verificar cuenta no bloqueada
 * 6. Generar tokens
 * 7. Registrar auditoría
 * 8. Retornar tokens + datos del usuario
 *
 * Seguridad:
 * - Mensajes de error genéricos (no revelar si email existe)
 * - Auditoría de intentos fallidos (detectar ataques)
 * - Timing attack resistant (BCrypt toma tiempo constante)
 */
@Service
@Transactional(readOnly = true)
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateUserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuditLogger auditLogger;

    public AuthenticateUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            AuditLogger auditLogger
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.auditLogger = auditLogger;
    }

    /**
     * Ejecuta el caso de uso de autenticación.
     *
     * @param command Comando con credenciales y contexto
     * @return AuthenticationResult con tokens y datos del usuario
     * @throws InvalidCredentialsException si email o password incorrectos
     * @throws AccountLockedException si la cuenta está bloqueada
     */
    @Override
    public AuthenticationResult execute(AuthenticateUserCommand command) {
        log.info("Attempting authentication for email: {}", command.email());

        try {
            //1: Buscar usuario por email
            User user = userRepository.findByEmail(command.email())
                    .orElseThrow(() -> {
                        auditLogger.logFailedAccess(
                                command.email(),
                                "USER_NOT_FOUND",
                                command.ipAddress()
                        );
                        // Mensaje genérico por seguridad
                        // No revelar si el email existe o no
                        return new InvalidCredentialsException("Invalid email or password");
                    });

            //2: Verificar contraseña
            if (!passwordEncoder.matches(command.password(), user.getPassword())) {
                auditLogger.logFailedAccess(
                        command.email(),
                        "INVALID_PASSWORD",
                        command.ipAddress()
                );

                log.warn("Invalid password attempt for user: {}", command.email());
                throw new InvalidCredentialsException("Invalid email or password");
            }

            //3: Verificar que la cuenta esté habilitada
            if (!user.isEnabled()) {
                auditLogger.logFailedAccess(
                        command.email(),
                        "ACCOUNT_DISABLED",
                        command.ipAddress()
                );

                log.warn("Attempt to login with disabled account: {}", command.email());

                throw new InvalidCredentialsException("Account is disabled");
            }

            //4: Verificar que la cuenta no esté bloqueada
            if (!user.isAccountNonLocked()) {
                auditLogger.logFailedAccess(
                        command.email(),
                        "ACCOUNT_LOCKED",
                        command.ipAddress()
                );

                log.warn("Attempt to login with locked account: {}", command.email());

                throw new AccountLockedException("Account is locked");
            }

            //5: Generar tokens JWT
            String accessToken = tokenService.generateAccessToken(user);
            String refreshToken = tokenService.generateRefreshToken(user);

            //6: Registrar auditoría de login exitoso
            auditLogger.logAction(
                    user.getId(),
                    "USER_LOGIN",
                    "USER:" + user.getEmail(),
                    command.ipAddress()
            );

            log.info("User authenticated successfully: {}", user.getEmail());

            //7 Retornar resultado
            return new AuthenticationResult(
                    accessToken,
                    refreshToken,
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
            );

        } catch (InvalidCredentialsException | AccountLockedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            throw new RuntimeException("Authenticated failed", e);
        }


    }
}
