package com.andy.iamapi.domain.port.input;

/**
 * Caso de uso: Autenticar usuario y generar tokens
 */
public interface AuthenticateUserUseCase {
    /**
     * Autentica un usuario y genera tokens JWT
     *
     * @param command Credenciales del usuario
     * @return Respuesta con tokens y datos del usuario
     * @throws InvalidCredentialsException si email/password incorrectos
     * @throws AccountLockedException si la cuenta está bloqueada
     */
    AuthenticationResult execute(AuthenticateUserCommand command);

    record AuthenticateUserCommand(
            String email,
            String password,
            String ipAddress //Auditoria
    ) {
        public AuthenticateUserCommand {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email cannot be empty");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password cannot be empty");
            }
        }
    }
    record AuthenticationResult(
            String accessToken,
            String refreshToken,
            String email,
            String firstName,
            String lastName
    ) {}
}
