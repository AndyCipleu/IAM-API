package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.InvalidCredentialsException;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.RefreshTokenUseCase;
import com.andy.iamapi.domain.port.output.TokenService;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para refrescar access tokens.
 *
 * Flujo:
 * 1. Validar refresh token
 * 2. Extraer email del token
 * 3. Buscar usuario en BD
 * 4. Generar nuevo access token
 * 5. Retornar nuevo access token
 *
 * Nota: NO genera nuevo refresh token (el refresh token sigue siendo válido)
 */
@Service
@Transactional(readOnly = true)
public class RefreshTokenService implements RefreshTokenUseCase {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public RefreshTokenService (UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    public RefreshTokenResult execute(RefreshTokenCommand command) {
        // PASO 1: Validar refresh token y extraer email
        String email = tokenService.validateTokenAndGetEmail(command.refreshToken())
                .orElseThrow(() -> {
                    log.warn("Invalid or expired refresh token");
                    throw new InvalidCredentialsException("Invalid or expired refresh token");
                });

        //PASO 2: Buscar usuario en BD
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for refresh token: {}", email);
                    throw new InvalidCredentialsException("Invalid or expired refresh token");
                });

        //PASO 3: Verificar que la cuenta esté habilitada
        if (!user.isEnabled()) {
            log.warn("Attempt to refresh token for disabled account: {}", email);
            throw new InvalidCredentialsException("Account is disabled");
        }

        //PASO 4: Verificar que la cuenta no esté bloqueada
        if (!user.isAccountNonLocked()) {
            log.warn("Attempt to refresh token for locked account: {}", email);
            throw new InvalidCredentialsException("Account is locked");
        }

        //PASO 5: Generar nuevo access token
        String newAccesToken = tokenService.generateAccessToken(user);
        log.info("Access token refreshed successfully for user: {}", user.getEmail());

        return new RefreshTokenResult (
                newAccesToken,
                3600L
        );
    }

}
