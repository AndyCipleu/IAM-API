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

// ===== Imports de OpenAPI/Swagger =====
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * Controller REST para endpoints de autenticación.
 *
 * Gestiona el ciclo de vida de autenticación de usuarios:
 * - Registro de nuevos usuarios
 * - Login y generación de tokens JWT
 * - Refresh de tokens expirados
 * - Logout y revocación de tokens
 *
 * Este controller es la CAPA de entrada REST (Infrastructure Layer).
 */
@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Endpoints de autenticación y gestión de tokens JWT. " +
                "Incluye registro, login, refresh y logout."
)
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
     * Endpoint público que no requiere autenticación previa.
     * Crea un usuario con rol ROLE_USER por defecto.
     * El email debe ser único en el sistema.
     * La contraseña debe cumplir: mínimo 8 caracteres, mayúsculas, minúsculas y números.
     *
     * @param request DTO con datos del nuevo usuario (email, password, firstName, lastName)
     * @return UserResponse con los datos del usuario creado (sin password)
     */
@PostMapping("/register")
@Operation(
        summary = "Registrar nuevo usuario",
        description = """
                Crea una nueva cuenta de usuario en el sistema.
                
                **Requisitos:**
                - Email válido y único
                - Password: mínimo 8 caracteres, incluir mayúsculas, minúsculas y números
                - firstName y lastName son obligatorios
                
                **Comportamiento:**
                - Usuario creado con rol ROLE_USER por defecto
                - Password hasheada con BCrypt antes de guardar
                - Usuario habilitado automáticamente (enabled=true)
                
                **Ejemplo de uso:**
                1. Llamar a este endpoint con los datos del usuario
                2. Si es exitoso, hacer login con el mismo email/password
                3. Usar el token recibido en login para acceder a endpoints protegidos
                """
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Usuario registrado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserResponse.class),
                        examples = @ExampleObject(
                                name = "Usuario creado",
                                value = """
                                    {
                                      "id": "a3c7ef12-9b4d-4f8a-b123-456789abcdef",
                                      "email": "john.doe@example.com",
                                      "firstName": "John",
                                      "lastName": "Doe",
                                      "enabled": true,
                                      "roles": ["ROLE_USER"]
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos (validación falló)",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Error de validación",
                                value = """
                                    {
                                      "status": 400,
                                      "message": "Validation failed",
                                      "errors": {
                                        "email": "Email inválido",
                                        "password": "La contraseña debe tener al menos 8 caracteres"
                                      }
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "El email ya está registrado",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Email duplicado",
                                value = """
                                    {
                                      "status": 409,
                                      "message": "Email already exists: john.doe@example.com"
                                    }
                                    """
                        )
                )
        )
})
@SecurityRequirement(name = "") //Enpoint público, no requiere de autenticación
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

    /**
     * Autentica un usuario y genera tokens JWT.
     *
     * Endpoint público que valida credenciales y retorna access + refresh tokens.
     *
     * @param request Credenciales del usuario (email y password)
     * @param httpRequest Request HTTP para extraer IP del cliente
     * @return AuthenticationResponse con tokens JWT y datos básicos del usuario
     */
@PostMapping("/login")
@Operation(
        summary = "Iniciar sesión",
        description = """
                Autentica un usuario con email y contraseña, retornando tokens JWT.
                
                **Flujo:**
                1. Valida que el usuario exista y esté habilitado
                2. Verifica la contraseña con BCrypt
                3. Genera un Access Token (válido 1 hora) y un Refresh Token (válido 7 días)
                4. Registra el login en los logs de auditoría con IP del cliente
                
                **Tokens generados:**
                - **Access Token**: Usar en header `Authorization: Bearer {token}` para endpoints protegidos
                - **Refresh Token**: Usar en `/api/auth/refresh` para obtener un nuevo access token
                
                **Seguridad:**
                - Las contraseñas nunca se transmiten sin hashear
                - Los tokens están firmados con HS256
                - La IP del cliente se registra para auditoría
                """
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Login exitoso, tokens generados",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AuthenticationResponse.class),
                        examples = @ExampleObject(
                                name = "Login exitoso",
                                value = """
                                    {
                                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                      "expiresIn": 3600,
                                      "user": {
                                        "email": "john.doe@example.com",
                                        "firstName": "John",
                                        "lastName": "Doe"
                                      }
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Validación fallida",
                                value = """
                                    {
                                      "status": 400,
                                      "message": "Validation failed",
                                      "errors": {
                                        "email": "Email no puede estar vacío"
                                      }
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Credenciales inválidas",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Credenciales incorrectas",
                                value = """
                                    {
                                      "status": 401,
                                      "message": "Invalid email or password"
                                    }
                                    """
                        )
                )
        )
})
@SecurityRequirement(name = "")  // Endpoint público
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
     * Renueva el access token usando un refresh token válido.
     *
     * Cuando el access token expira (1 hora), usa este endpoint
     * para obtener uno nuevo sin hacer login de nuevo.
     *
     * @param request DTO con el refresh token
     * @return RefreshTokenResponse con nuevo access token
     */
@PostMapping("/refresh")
@Operation(
        summary = "Renovar access token",
        description = """
                Genera un nuevo access token usando un refresh token válido.
                
                **Cuándo usar:**
                - Cuando el access token haya expirado (después de 1 hora)
                - Para evitar que el usuario tenga que hacer login de nuevo
                
                **Comportamiento:**
                - Valida que el refresh token sea válido y no esté en blacklist
                - Genera un nuevo access token con la misma información del usuario
                - El refresh token sigue siendo válido (no se renueva)
                - El nuevo access token expira en 1 hora
                
                **Nota:** Los refresh tokens expiran en 7 días. Después de eso,
                el usuario debe hacer login de nuevo.
                """
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Token renovado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = RefreshTokenResponse.class),
                        examples = @ExampleObject(
                                name = "Token renovado",
                                value = """
                                    {
                                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                      "expiresIn": 3600
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Refresh token inválido o expirado",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Token inválido",
                                value = """
                                    {
                                      "status": 401,
                                      "message": "Invalid or expired refresh token"
                                    }
                                    """
                        )
                )
        )
})
@SecurityRequirement(name = "")  // Endpoint público (usa refresh token en body)
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
     * Invalida tanto el access token como el refresh token
     * añadiéndolos a la blacklist en Redis.
     *
     * @param request DTO con access y refresh tokens a revocar
     * @return 204 No Content si es exitoso
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = """
                    Invalida los tokens del usuario, cerrando su sesión.
                    
                    **Comportamiento:**
                    - Añade access token a la blacklist en Redis (TTL: 1 hora)
                    - Añade refresh token a la blacklist en Redis (TTL: 7 días)
                    - Los tokens quedan inmediatamente inválidos
                    - Cualquier petición con esos tokens será rechazada
                    
                    **Después del logout:**
                    - El usuario debe hacer login de nuevo para obtener nuevos tokens
                    - Los tokens antiguos NO pueden ser usados nunca más
                    
                    **Seguridad:**
                    - Requiere autenticación (access token válido en header)
                    - Solo el usuario autenticado puede hacer logout de su sesión
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout exitoso, tokens revocados"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado o token inválido",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "No autenticado",
                                    value = """
                                        {
                                          "status": 401,
                                          "message": "Full authentication is required"
                                        }
                                        """
                            )
                    )
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")  // Requiere autenticación JWT
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
