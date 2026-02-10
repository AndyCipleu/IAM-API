package com.andy.iamapi.infrastructure.adapter.rest.controller;

import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.AuthenticateUserUseCase;
import com.andy.iamapi.domain.port.input.AuthenticateUserUseCase.AuthenticationResult;
import com.andy.iamapi.domain.port.input.AuthenticateUserUseCase.AuthenticateUserCommand;
import com.andy.iamapi.domain.port.input.LogoutUseCase.LogoutCommand;
import com.andy.iamapi.domain.port.input.LogoutUseCase;
import com.andy.iamapi.domain.port.input.RefreshTokenUseCase.RefreshTokenResult;
import com.andy.iamapi.domain.port.input.RefreshTokenUseCase;
import com.andy.iamapi.domain.port.input.RefreshTokenUseCase.RefreshTokenCommand;
import com.andy.iamapi.domain.port.input.RegisterUserUseCase;
import com.andy.iamapi.infrastructure.adapter.rest.dto.request.LoginRequest;
import com.andy.iamapi.infrastructure.adapter.rest.dto.request.LogoutRequest;
import com.andy.iamapi.infrastructure.adapter.rest.dto.request.RefreshTokenRequest;
import com.andy.iamapi.infrastructure.adapter.rest.dto.request.RegisterUserRequest;
import com.andy.iamapi.domain.port.input.RegisterUserUseCase.RegisterUserCommand;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.AuthenticationResponse;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.RefreshTokenResponse;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller REST para endpoints de autenticación.
 *
 * Endpoints:
 * - POST /api/auth/register - Registrar nuevo usuario
 * - POST /api/auth/login - Autenticar usuario (TODO: implementar cuando tengamos JWT)
 * - POST /api/auth/refresh - Refrescar token (TODO: implementar)
 * - POST /api/auth/logout - Cerrar sesión (TODO: implementar)
 *
 * Este controller es la CAPA de entrada REST.
 * Responsabilidades:
 * - Recibir requests HTTP
 * - Validar DTOs (automático con Valid)
 * - Convertir DTOs a Commands
 * - Llamar a los use cases (puertos)
 * - Convertir domain models a DTOs de respuesta
 * - Retornar responses HTTP
 *
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegisterUserUseCase registerUserUseCase;

    private final AuthenticateUserUseCase authenticateUserUseCase;

    private final RefreshTokenUseCase refreshTokenUseCase;

    private final LogoutUseCase logoutUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

/**
 * Registra un nuevo usuario en el sistema.
 *
 * Endpoint: POST /api/auth/register
 *
 * Request body (JSON):
 * {
 *   "email": "john@example.com",
 *   "password": "Password123",
 *   "firstName": "John",
 *   "lastName": "Doe"
 * }
 *
 * Response exitosa (201 Created):
 * {
 *   "id": "uuid-123",
 *   "email": "john@example.com",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "enabled": true,
 *   "roles": ["ROLE_USER"],
 *   "createdAt": "2026-02-02T15:30:45"
 * }
 *
 * Errores posibles:
 * - 400 Bad Request: Validación falló (email inválido, password corta)
 * - 409 Conflict: Email ya existe
 *
 * Valid: Ejecuta validaciones de Bean Validation del DTO
 * Si falla, Spring lanza MethodArgumentNotValidException
 * (capturada por GlobalExceptionHandler)
 *
 * RequestBody: Convierte JSON del request a RegisterUserRequest automáticamente
 *
 * @param request DTO con datos del nuevo usuario
 * @return ResponseEntity con UserResponse y status 201
 */
@PostMapping("/register")
    public ResponseEntity<UserResponse> register(
        @Valid @RequestBody RegisterUserRequest request
        ) {

    log.info("Registering new user with email: {}", request.email());

    //Paso 1: Convertir DTO(Request) a Command(Domain)
    RegisterUserCommand command = request.toCommand();

    //Paso 2: Ejecutar caso de uso (lógica de negocio)
    User user = registerUserUseCase.execute(command);

    //Paso 3: Convertir Domain Model a DTO (response)
    UserResponse response = UserResponse.fromDomain(user);

    log.info("User registered succesfully with ID: {}", user.getId());

    //Paso 4: Retornar ResponseEntity con status 201 Created
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
}

@PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
        ) {

    //1. Crear command
    AuthenticateUserCommand command = new AuthenticateUserCommand(
            request.email(),
            request.password(),
            getClientIp(httpRequest)
    );

    //2. Ejecutar service -> recibe AuthenticationResult(domain)
    AuthenticationResult result = authenticateUserUseCase.execute(command);

    //3. Convertir a AuthenticationResponse(DTO)
    UserResponse userResponse = new UserResponse(
            null,
            result.email(),
            result.firstName(),
            result.lastName(),
            null,
            null
    );

    AuthenticationResponse response = new AuthenticationResponse(
            result.accessToken(),
            result.refreshToken(),
            3600L,
            userResponse
    );

    return ResponseEntity.ok(response);
}

/**
 * Refresca el access token usando un refresh token válido.
 *
 * Endpoint: POST /api/auth/refresh
 *
 * Request body:
 * {
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
 * }
 *
 * Response exitosa (200 OK):
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIs...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 3600
 * }
 *
 * Errores posibles:
 * - 400 Bad Request: Validación falló
 * - 401 Unauthorized: Refresh token inválido/expirado
 */
@PostMapping("/refresh")
public ResponseEntity<RefreshTokenResponse> refresh (
        @Valid @RequestBody RefreshTokenRequest request
        ) {

    log.info("Attempting to refresh access token");

    RefreshTokenCommand command = new RefreshTokenCommand(request.refreshToken());

    RefreshTokenResult result = refreshTokenUseCase.execute(command);

    RefreshTokenResponse response = new RefreshTokenResponse(
            result.accessToken(),
            result.expiresIn()
    );

    log.info("Access token refreshed successfully");

    return ResponseEntity
            .ok(response);

}

    /**
     * Cierra la sesión del usuario revocando sus tokens.
     *
     * Endpoint: POST /api/auth/logout
     *
     * Request body:
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIs...",
     *   "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
     * }
     *
     * Response exitosa (204 No Content):
     * (sin body)
     *
     * Después del logout:
     * - Access token agregado a blacklist (no se puede usar más)
     * - Refresh token agregado a blacklist (no se puede usar más)
     * - Usuario debe hacer login de nuevo
     *
     * Este endpoint REQUIERE autenticación (token válido en header).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request
            ) {

        //Obtener el usuario autenticado del SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        log.info("Logging out for user: {}", user.getId());

        LogoutCommand command = new LogoutCommand(
                user.getId(),
                request.accessToken(),
                request.refreshToken()
        );

        logoutUseCase.execute(command);

        log.info("User logged out successfully: {}", user.getId());

        return ResponseEntity.noContent().build();
    }



    /**
     * Extrae la IP del cliente del request.
     *
     * Considera proxies y load balancers (X-Forwarded-For header).
     *
     * @param request HttpServletRequest
     * @return IP del cliente
     */
    private String getClientIp(HttpServletRequest request) {
        // Si está detrás de un proxy/load balancer
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For puede contener: "client_ip, proxy1_ip, proxy2_ip"
            // Tomamos la primera (IP del cliente real)
            return xForwardedFor.split(",")[0].trim();
        }

        // Si no hay proxy, usar IP directa
        return request.getRemoteAddr();
    }






}
