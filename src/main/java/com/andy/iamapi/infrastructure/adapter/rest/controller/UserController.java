package com.andy.iamapi.infrastructure.adapter.rest.controller;

import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.*;
import com.andy.iamapi.domain.port.input.AssignRoleToUserUseCase;
import com.andy.iamapi.domain.port.input.GetAllUsersWithPaginationUseCase.GetUsersCommand;
import com.andy.iamapi.domain.port.input.GetAllUsersWithPaginationUseCase;
import com.andy.iamapi.domain.port.input.RevokeRoleFromUserUseCase;
import com.andy.iamapi.domain.port.input.RevokeRoleFromUserUseCase.RevokeRoleCommand;
import com.andy.iamapi.domain.port.input.AssignRoleToUserUseCase.AssignRoleCommand;
import com.andy.iamapi.infrastructure.adapter.rest.dto.request.ChangePasswordRequest;
import com.andy.iamapi.infrastructure.adapter.rest.dto.request.UpdateUserRequest;
import com.andy.iamapi.infrastructure.adapter.rest.dto.request.UserFilterRequest;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.PageResponse;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.UserListResponse;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
 * Controller REST para endpoints de gestión de usuarios.
 *
 * Gestiona operaciones CRUD sobre usuarios y asignación de roles:
 * - Consultar perfil propio
 * - Listar todos los usuarios (solo admins)
 * - Obtener usuario por ID (solo admins)
 * - Actualizar datos de usuario (admin o mismo usuario)
 * - Cambiar contraseña (solo mismo usuario)
 * - Eliminar usuario (solo admins)
 * - Asignar/revocar roles (solo admins)
 *
 * Todos los endpoints requieren autenticación JWT.
 * Algunos endpoints tienen restricciones adicionales de autorización (ROLE_ADMIN).
 */
@RestController
@RequestMapping("/api/users")
@Tag(
        name = "Users",
        description = "Gestión completa de usuarios del sistema. " +
                "Incluye CRUD de usuarios, cambio de contraseña y asignación de roles. " +
                "La mayoría de operaciones requieren rol ROLE_ADMIN."
)
@SecurityRequirement(name = "Bearer Authentication")  // Todos los endpoints requieren JWT
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final GetAllUsersWithPaginationUseCase getAllUsersWithPaginationUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    private final AssignRoleToUserUseCase assignRoleToUserUseCase;
    private final RevokeRoleFromUserUseCase revokeRoleFromUserUseCase;

    public UserController(
            GetCurrentUserUseCase getCurrentUserUseCase,
            GetAllUsersWithPaginationUseCase getAllUsersWithPaginationUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            UpdateUserUseCase updateUserUseCase,
            ChangePasswordUseCase changePasswordUseCase,
            DeleteUserUseCase deleteUserUseCase,
            AssignRoleToUserUseCase assignRoleToUserUseCase,
            RevokeRoleFromUserUseCase revokeRoleFromUserUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getAllUsersWithPaginationUseCase = getAllUsersWithPaginationUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.assignRoleToUserUseCase = assignRoleToUserUseCase;
        this.revokeRoleFromUserUseCase = revokeRoleFromUserUseCase;
    }

    /**
     * Obtiene el perfil del usuario autenticado actualmente.
     *
     * Endpoint útil para que la UI obtenga los datos del usuario logueado
     * sin necesidad de saber su ID. Extrae el usuario del token JWT.
     *
     * @return UserResponse con los datos del usuario autenticado
     */
    @GetMapping("/me")
    @Operation(
            summary = "Obtener mi perfil",
            description = """
                    Retorna los datos del usuario actualmente autenticado.
                    
                    **Comportamiento:**
                    - Extrae el usuario del token JWT en el header Authorization
                    - No necesitas proporcionar el ID del usuario
                    - Útil para mostrar perfil en la UI después del login
                    
                    **Casos de uso:**
                    - Dashboard: mostrar nombre del usuario logueado
                    - Perfil: cargar datos para editar
                    - Validación: verificar roles/permisos del usuario actual
                    
                    **Seguridad:**
                    - Requiere token JWT válido
                    - Solo puede ver sus propios datos
                    - No requiere rol específico (cualquier usuario autenticado)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Perfil de usuario",
                                    value = """
                                        {
                                          "id": "a3c7ef12-9b4d-4f8a-b123-456789abcdef",
                                          "email": "john.doe@example.com",
                                          "firstName": "John",
                                          "lastName": "Doe",
                                          "enabled": true,
                                          "roles": ["ROLE_USER", "ROLE_ADMIN"]
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT inválido o expirado",
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
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        log.debug("Getting current user profile: {}", currentUser.getId());

        User user = getCurrentUserUseCase.execute(currentUser.getId());

        UserResponse response = UserResponse.fromDomain(user);

        return ResponseEntity.ok(response);
    }

    /**
     * Lista usuarios con paginación y filtros opcionales.
     *
     * Endpoint que permite buscar usuarios aplicando múltiples filtros
     * y retornar resultados paginados para mejor rendimiento.
     *
     * Requiere rol ROLE_ADMIN.
     *
     * @param filters Filtros opcionales (email, firstName, lastName, enabled, role)
     * @param pageable Configuración de paginación (page, size, sort)
     * @return PageResponse con los usuarios encontrados y metadata de paginación
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Listar usuarios con paginación y filtros",
            description = """
                    Obtiene una lista paginada de usuarios con filtros opcionales.
                    
                    **Paginación:**
                    - `page`: Número de página (0-indexed, default: 0)
                    - `size`: Elementos por página (default: 10, max: 100)
                    - `sort`: Campo y dirección (ej: email,asc o createdAt,desc)
                    
                    **Filtros disponibles (todos opcionales):**
                    - `email`: Búsqueda parcial case-insensitive (ej: "john" encuentra "john@test.com")
                    - `firstName`: Búsqueda parcial case-insensitive
                    - `lastName`: Búsqueda parcial case-insensitive
                    - `enabled`: true/false para filtrar por estado
                    - `role`: Nombre exacto del rol (ej: ROLE_ADMIN, ROLE_USER)
                    
                    **Ejemplos de uso:**
                    - `/api/users?page=0&size=10` - Primera página, 10 elementos
                    - `/api/users?page=1&size=20&sort=email,asc` - Segunda página, ordenado por email
                    - `/api/users?email=john&enabled=true` - Usuarios habilitados con "john" en el email
                    - `/api/users?role=ROLE_ADMIN` - Solo administradores
                    - `/api/users?firstName=John&lastName=Doe&role=ROLE_USER` - Múltiples filtros
                    
                    **Ordenamiento:**
                    Puedes ordenar por: email, firstName, lastName, createdAt, enabled
                    Dirección: asc (ascendente) o desc (descendente)
                    
                    **Casos de uso:**
                    - Panel de administración con búsqueda y paginación
                    - Exportar usuarios filtrados por criterios específicos
                    - Buscar usuarios por nombre/email para asignar roles
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista paginada obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class),
                            examples = @ExampleObject(
                                    name = "Página de usuarios",
                                    value = """
                                        {
                                          "content": [
                                            {
                                              "id": "a3c7ef12-9b4d-4f8a-b123-456789abcdef",
                                              "email": "admin@example.com",
                                              "firstName": "Admin",
                                              "lastName": "User",
                                              "enabled": true,
                                              "roles": ["ROLE_USER", "ROLE_ADMIN"]
                                            },
                                            {
                                              "id": "b4d8fg23-0c5e-13e4-c234-567890bcdefg",
                                              "email": "john.doe@example.com",
                                              "firstName": "John",
                                              "lastName": "Doe",
                                              "enabled": true,
                                              "roles": ["ROLE_USER"]
                                            }
                                          ],
                                          "page": {
                                            "size": 10,
                                            "number": 0,
                                            "totalElements": 45,
                                            "totalPages": 5
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de paginación inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error de paginación",
                                    value = """
                                        {
                                          "status": 400,
                                          "message": "Page index must not be less than zero"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (no es ROLE_ADMIN)"
            )
    })
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @Parameter(hidden = true)  // Los filtros se documentan en UserFilterRequest
            UserFilterRequest filters,
            @Parameter(
                    description = "Configuración de paginación y ordenamiento",
                    examples = {
                            @ExampleObject(name = "Primera página", value = "page=0&size=10"),
                            @ExampleObject(name = "Ordenar por email", value = "sort=email,asc"),
                            @ExampleObject(name = "Completo", value = "page=1&size=20&sort=createdAt,desc")
                    }
            )
            Pageable pageable
    ) {
        log.debug("Getting users with pagination - page: {}, size: {}, sort: {}, filters: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort(),
                filters
        );

        // 1. Crear comando con filtros y paginación
        GetUsersCommand command = new GetUsersCommand(
                filters.email(),
                filters.firstName(),
                filters.lastName(),
                filters.enabled(),
                filters.role(),
                pageable
        );

        // 2. Ejecutar caso de uso (retorna Page<User> del dominio)
        Page<User> userPage = getAllUsersWithPaginationUseCase.execute(command);

        // 3. Mapear Page<User> → Page<UserResponse> (DTOs)
        Page<UserResponse> responsePage = userPage.map(UserResponse::fromDomain);

        // 4. Envolver en PageResponse (nuestro DTO de respuesta)
        PageResponse<UserResponse> response = PageResponse.fromPage(responsePage);

        log.debug("Returning {} users from page {} of {}",
                response.content().size(),
                response.page().number(),
                response.page().totalPages()
        );

        return ResponseEntity.ok(response);

    }

    /**
     * Obtiene un usuario específico por su ID.
     *
     * Endpoint restringido solo para administradores.
     *
     * @param id UUID del usuario a buscar
     * @return UserResponse con los datos del usuario
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Obtener usuario por ID",
            description = """
                    Busca y retorna un usuario específico por su identificador único.
                    
                    **Restricciones:**
                    - Solo accesible por usuarios con rol ROLE_ADMIN
                    
                    **Casos de uso:**
                    - Ver detalles completos de un usuario específico
                    - Cargar datos para edición en panel de admin
                    - Auditoría de cuenta específica
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Usuario encontrado",
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
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Usuario no existe",
                                    value = """
                                        {
                                          "status": 404,
                                          "message": "User not found with id: a3c7ef12-9b4d-4f8a-b123-456789abcdef"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (no es ROLE_ADMIN)"
            )
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.debug("Getting user by ID: {}", id);

        User user = getUserByIdUseCase.execute(id);

        UserResponse response = UserResponse.fromDomain(user);

        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza los datos de un usuario.
     *
     * Permite actualizar firstName, lastName y email.
     * Requiere ser ROLE_ADMIN o ser el mismo usuario.
     *
     * @param id UUID del usuario a actualizar
     * @param request DTO con los nuevos datos
     * @return UserResponse con los datos actualizados
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.id")
    @Operation(
            summary = "Actualizar usuario",
            description = """
                    Actualiza los datos personales de un usuario.
                    
                    **Campos actualizables:**
                    - firstName (nombre)
                    - lastName (apellido)
                    - email (debe ser único si se cambia)
                    
                    **Autorización:**
                    - ROLE_ADMIN: Puede actualizar cualquier usuario
                    - Usuario normal: Solo puede actualizar su propio perfil
                    
                    **Validaciones:**
                    - Email debe ser válido y único en el sistema
                    - Si el email ya existe (y es de otro usuario), retorna 409 Conflict
                    
                    **Nota:** Para cambiar la contraseña, usar el endpoint específico
                    PUT /api/users/{id}/password
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Usuario actualizado",
                                    value = """
                                        {
                                          "id": "a3c7ef12-9b4d-4f8a-b123-456789abcdef",
                                          "email": "john.updated@example.com",
                                          "firstName": "John Updated",
                                          "lastName": "Doe Updated",
                                          "enabled": true,
                                          "roles": ["ROLE_USER"]
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
                                    value = """
                                        {
                                          "status": 400,
                                          "message": "Validation failed",
                                          "errors": {
                                            "email": "Email inválido"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos para actualizar este usuario"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El nuevo email ya está en uso por otro usuario",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "status": 409,
                                          "message": "Email already exists: john.updated@example.com"
                                        }
                                        """
                            )
                    )
            )
    })
    public ResponseEntity<UserResponse> updateUser (
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating user: {}", id);

        User updatedUser = updateUserUseCase.execute(request.toCommand(id));

        UserResponse response = UserResponse.fromDomain(updatedUser);

        log.info("User updated successfully: {}", id);

        return ResponseEntity.ok(response);
    }


    /**
     * Cambia la contraseña de un usuario.
     *
     * Requiere la contraseña actual para validación.
     * Solo el mismo usuario puede cambiar su contraseña (no admins).
     *
     * @param id UUID del usuario
     * @param request DTO con contraseña actual y nueva
     * @return 204 No Content si es exitoso
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("#id == authentication.principal.id")
    @Operation(
            summary = "Cambiar contraseña",
            description = """
                    Permite al usuario cambiar su propia contraseña.
                    
                    **Seguridad:**
                    - Requiere la contraseña actual para confirmar identidad
                    - Solo el propio usuario puede cambiar su contraseña
                    - Ni siquiera ROLE_ADMIN puede cambiar contraseñas de otros
                    
                    **Validaciones:**
                    - La contraseña actual debe ser correcta
                    - La nueva contraseña debe cumplir requisitos de seguridad:
                      * Mínimo 8 caracteres
                      * Al menos una mayúscula
                      * Al menos una minúscula
                      * Al menos un número
                    
                    **Proceso:**
                    1. Se valida la contraseña actual con BCrypt
                    2. Se valida que la nueva contraseña cumpla requisitos
                    3. Se hashea la nueva contraseña con BCrypt
                    4. Se actualiza en la base de datos
                    5. Los tokens JWT actuales siguen siendo válidos
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Contraseña cambiada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Contraseña actual incorrecta o nueva contraseña inválida",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Contraseña actual incorrecta",
                                            value = """
                                                {
                                                  "status": 400,
                                                  "message": "Current password is incorrect"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "Nueva contraseña inválida",
                                            value = """
                                                {
                                                  "status": 400,
                                                  "message": "Password must be at least 8 characters and contain uppercase, lowercase and numbers"
                                                }
                                                """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tienes permiso para cambiar la contraseña de otro usuario"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request
            ) {

        log.info("Changing password for user: {}", id);

        changePasswordUseCase.execute(request.toCommand(id));

        log.info("Password changed successfully for user: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina un usuario del sistema.
     *
     * Endpoint restringido solo para administradores.
     *
     * @param id UUID del usuario a eliminar
     * @return 204 No Content si es exitoso
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Eliminar usuario",
            description = """
        Elimina un usuario del sistema de forma permanente.
        
        **Restricciones:**
        - Solo accesible por usuarios con rol ROLE_ADMIN
        - No se puede eliminar a sí mismo (validación en backend)
        
        **Comportamiento:**
        - Eliminación física de la base de datos (hard delete)
        - Se eliminan en cascada:
          * Asignaciones de roles del usuario
          * Referencias en otras tablas
        
        **Consideraciones:**
        - Esta operación NO es reversible
        - En producción considera usar "soft delete" (enabled=false)
        - Los tokens del usuario eliminado quedan en blacklist hasta expirar
        
        **Alternativa más segura:**
        - Deshabilitar usuario (enabled=false) en lugar de eliminar
        - Permite auditoría y recuperación de cuenta
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede eliminar (ej: intentando eliminarse a sí mismo)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "status": 400,
                                          "message": "Cannot delete yourself"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (no es ROLE_ADMIN)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("Deleting user: {}", id);

        deleteUserUseCase.execute(id);

        log.info("User deleted successfully: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Asigna un rol a un usuario.
     *
     * Endpoint restringido solo para administradores.
     *
     * @param userId UUID del usuario
     * @param roleId UUID del rol a asignar
     * @return 204 No Content si es exitoso
     */
    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Asignar rol a usuario",
            description = """
                    Añade un rol adicional a un usuario existente.
                    
                    **Restricciones:**
                    - Solo accesible por usuarios con rol ROLE_ADMIN
                    
                    **Comportamiento:**
                    - Si el usuario ya tiene ese rol, no hace nada (idempotente)
                    - El usuario mantiene sus roles anteriores
                    - Los cambios NO afectan a tokens JWT ya emitidos
                    - El usuario debe hacer login de nuevo para que sus tokens reflejen el nuevo rol
                    
                    **Casos de uso:**
                    - Promover usuario a moderador o admin
                    - Añadir permisos temporales
                    - Gestión de accesos desde panel de administración
                    
                    **Roles disponibles:**
                    - ROLE_USER (rol básico, asignado automáticamente en registro)
                    - ROLE_MODERATOR (permisos intermedios)
                    - ROLE_ADMIN (acceso completo al sistema)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Rol asignado exitosamente al usuario"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (no es ROLE_ADMIN)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario o rol no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Usuario no encontrado",
                                            value = """
                                                {
                                                  "status": 404,
                                                  "message": "User not found with id: a3c7ef12-9b4d-4f8a-b123-456789abcdef"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "Rol no encontrado",
                                            value = """
                                                {
                                                  "status": 404,
                                                  "message": "Role not found with id: b4d8fg23-0c5e-13e4-c234-567890bcdefg"
                                                }
                                                """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        log.info("Assigning role {} to user {}", roleId, userId);

        AssignRoleCommand command = new AssignRoleCommand(userId, roleId);

        assignRoleToUserUseCase.execute(command);

        log.info("Role assigned successfully");

        return ResponseEntity.noContent().build();
    }


    /**
     * Revoca un rol de un usuario.
     *
     * Endpoint restringido solo para administradores.
     *
     * @param userId UUID del usuario
     * @param roleId UUID del rol a revocar
     * @return 204 No Content si es exitoso
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Revocar rol de usuario",
            description = """
                    Elimina un rol específico de un usuario.
                    
                    **Restricciones:**
                    - Solo accesible por usuarios con rol ROLE_ADMIN
                    
                    **Comportamiento:**
                    - Elimina la asignación del rol al usuario
                    - Si el usuario no tiene ese rol, no hace nada (idempotente)
                    - El usuario mantiene sus otros roles
                    - Los tokens JWT actuales siguen teniendo el rol hasta que expiren
                    - El usuario debe hacer login de nuevo para reflejar el cambio
                    
                    **Validaciones:**
                    - No se puede revocar ROLE_USER si es el único rol del usuario
                    - Esto previene usuarios sin ningún rol en el sistema
                    
                    **Casos de uso:**
                    - Remover permisos de administrador
                    - Degradar moderadores a usuarios normales
                    - Gestión de accesos desde panel de administración
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Rol revocado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede revocar el último rol del usuario",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "status": 400,
                                          "message": "Cannot remove the last role from user"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (no es ROLE_ADMIN)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario o rol no encontrado"
            )
    })
    public ResponseEntity<Void> revokeRoleFromUser (
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        log.info("Revoking role {} from user {}", roleId, userId);

        RevokeRoleCommand command = new RevokeRoleCommand(userId, roleId);

        revokeRoleFromUserUseCase.execute(command);

        log.info("Role revoked successfully");

        return ResponseEntity.noContent().build();
    }
}
