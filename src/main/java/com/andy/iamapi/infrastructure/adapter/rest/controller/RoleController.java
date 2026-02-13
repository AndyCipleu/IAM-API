package com.andy.iamapi.infrastructure.adapter.rest.controller;

import com.andy.iamapi.domain.model.Role;
import com.andy.iamapi.domain.port.input.AssignRoleToUserUseCase.AssignRoleCommand;
import com.andy.iamapi.domain.port.input.AssignRoleToUserUseCase;
import com.andy.iamapi.domain.port.input.GetAllRolesUseCase;
import com.andy.iamapi.domain.port.input.GetRoleByIdUseCase;
import com.andy.iamapi.domain.port.input.RevokeRoleFromUserUseCase.RevokeRoleCommand;
import com.andy.iamapi.domain.port.input.RevokeRoleFromUserUseCase;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.RoleListResponse;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.RoleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
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
 * Controller REST para endpoints de gestión de roles.
 *
 * Los roles definen conjuntos de permisos que se asignan a usuarios.
 * En este sistema, los roles están predefinidos en la base de datos
 * mediante migraciones de Flyway y no son modificables dinámicamente.
 *
 * Endpoints disponibles:
 * - GET /api/roles - Listar todos los roles del sistema
 * - GET /api/roles/{id} - Obtener un rol específico por ID
 *
 * Todos los endpoints requieren autenticación JWT y rol ROLE_ADMIN.
 *
 * Roles predefinidos en el sistema:
 * - ROLE_USER: Usuario estándar del sistema
 * - ROLE_MODERATOR: Moderador con permisos intermedios
 * - ROLE_ADMIN: Administrador con acceso completo
 */
@RestController
@RequestMapping("/api/roles")
@Tag(
        name = "Roles",
        description = "Consulta de roles y permisos del sistema. " +
                "Los roles están predefinidos en la base de datos y no se pueden " +
                "crear/editar/eliminar dinámicamente (definidos en migraciones Flyway). " +
                "Todos los endpoints requieren ROLE_ADMIN."
)
@SecurityRequirement(name = "Bearer Authentication")  // Todos requieren JWT
public class RoleController {
    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    private final GetAllRolesUseCase getAllRolesUseCase;
    private final GetRoleByIdUseCase getRoleByIdUseCase;

    public RoleController(
            GetAllRolesUseCase getAllRolesUseCase,
            GetRoleByIdUseCase getRoleByIdUseCase) {
        this.getAllRolesUseCase = getAllRolesUseCase;
        this.getRoleByIdUseCase = getRoleByIdUseCase;
    }

    /**
     * Obtiene todos los roles del sistema.
     *
     * Lista completa de roles predefinidos con sus permisos asociados.
     * Útil para:
     * - Mostrar roles disponibles en UI de administración
     * - Conocer qué permisos tiene cada rol
     * - Asignar roles a usuarios (necesitas los IDs de roles)
     *
     * @return RoleListResponse con todos los roles y sus permisos
     */
    @GetMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Listar todos los roles",
            description = """
                    Retorna la lista completa de roles definidos en el sistema.
                    
                    **Información incluida por cada rol:**
                    - ID único del rol (UUID)
                    - Nombre del rol (ej: ROLE_ADMIN, ROLE_USER)
                    - Descripción del rol
                    - Lista de permisos asociados al rol
                    
                    **Roles disponibles en el sistema:**
                    - **ROLE_USER**: Rol básico asignado a todos los usuarios
                      * Permisos: Leer y actualizar su propio perfil
                    - **ROLE_MODERATOR**: Rol intermedio con permisos adicionales
                      * Permisos: Moderación de contenido, gestión limitada de usuarios
                    - **ROLE_ADMIN**: Rol con acceso completo al sistema
                      * Permisos: Acceso total a usuarios, roles y configuración
                    
                    **Casos de uso:**
                    - Panel de administración: Mostrar roles disponibles para asignar
                    - Formularios: Dropdown con roles existentes
                    - Auditoría: Ver qué permisos tiene cada rol
                    
                    **Nota importante:**
                    Los roles están definidos en migraciones de base de datos (Flyway)
                    y no se pueden crear/modificar/eliminar desde la API.
                    Esto garantiza consistencia y seguridad en el sistema de permisos.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de roles obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoleListResponse.class),
                            examples = @ExampleObject(
                                    name = "Lista de roles",
                                    value = """
                                        {
                                          "roles": [
                                            {
                                              "id": "c5e9gh34-1d6f-24f5-d345-678901cdefgh",
                                              "name": "ROLE_USER",
                                              "description": "Usuario estándar del sistema",
                                              "permissions": [
                                                "READ_PROFILE",
                                                "UPDATE_PROFILE"
                                              ]
                                            },
                                            {
                                              "id": "d6f0hi45-2e7g-35g6-e456-789012defghi",
                                              "name": "ROLE_MODERATOR",
                                              "description": "Moderador con permisos intermedios",
                                              "permissions": [
                                                "READ_PROFILE",
                                                "UPDATE_PROFILE",
                                                "READ_USERS",
                                                "MODERATE_CONTENT"
                                              ]
                                            },
                                            {
                                              "id": "e7g1ij56-3f8h-46h7-f567-890123efghij",
                                              "name": "ROLE_ADMIN",
                                              "description": "Administrador con acceso completo",
                                              "permissions": [
                                                "READ_PROFILE",
                                                "UPDATE_PROFILE",
                                                "READ_USERS",
                                                "CREATE_USERS",
                                                "UPDATE_USERS",
                                                "DELETE_USERS",
                                                "MANAGE_ROLES"
                                              ]
                                            }
                                          ],
                                          "total": 3
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado (token inválido o ausente)",
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (usuario no tiene ROLE_ADMIN)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Acceso denegado",
                                    value = """
                                        {
                                          "status": 403,
                                          "message": "Access Denied"
                                        }
                                        """
                            )
                    )
            )
    })
    public ResponseEntity<RoleListResponse> getAllRoles () {
        log.debug("Getting all roles");

        Set<Role> roles = getAllRolesUseCase.execute();

        RoleListResponse roleResponses = RoleListResponse.fromDomain(roles);

        return ResponseEntity.ok(roleResponses);
    }

    /**
     * Obtiene un rol específico por su ID.
     *
     * Útil para:
     * - Ver detalles completos de un rol antes de asignarlo
     * - Verificar qué permisos tiene un rol específico
     * - Cargar datos de un rol en formularios de edición de usuario
     *
     * @param id UUID del rol a buscar
     * @return RoleResponse con los datos del rol y sus permisos
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Obtener rol por ID",
            description = """
                    Busca y retorna un rol específico por su identificador único.
                    
                    **Información retornada:**
                    - ID único del rol (UUID)
                    - Nombre del rol (ej: ROLE_ADMIN)
                    - Descripción del propósito del rol
                    - Lista completa de permisos asociados
                    
                    **Casos de uso:**
                    - Ver detalles antes de asignar rol a usuario
                    - Verificar permisos de un rol específico
                    - Validar si un rol tiene el permiso necesario
                    - Mostrar información del rol en UI de administración
                    
                    **Restricciones:**
                    - Solo accesible por usuarios con rol ROLE_ADMIN
                    - El rol debe existir en la base de datos (predefinidos en Flyway)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rol encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoleResponse.class),
                            examples = @ExampleObject(
                                    name = "Rol ADMIN encontrado",
                                    value = """
                                        {
                                          "id": "e7g1ij56-3f8h-46h7-f567-890123efghij",
                                          "name": "ROLE_ADMIN",
                                          "description": "Administrador con acceso completo al sistema",
                                          "permissions": [
                                            "READ_PROFILE",
                                            "UPDATE_PROFILE",
                                            "READ_USERS",
                                            "CREATE_USERS",
                                            "UPDATE_USERS",
                                            "DELETE_USERS",
                                            "MANAGE_ROLES",
                                            "VIEW_AUDIT_LOGS"
                                          ]
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado (token inválido o ausente)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "status": 401,
                                          "message": "Full authentication is required"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (usuario no tiene ROLE_ADMIN)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "status": 403,
                                          "message": "Access Denied"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol no encontrado con el ID proporcionado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Rol no existe",
                                    value = """
                                        {
                                          "status": 404,
                                          "message": "Role not found with id: e7g1ij56-3f8h-46h7-f567-890123efghij"
                                        }
                                        """
                            )
                    )
            )
    })
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable UUID id) {
        log.debug("Getting role by ID: {}", id);

        Role role = getRoleByIdUseCase.execute(id);

        RoleResponse response = RoleResponse.fromDomain(role);

        return ResponseEntity.ok(response);
    }



}
