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

/**
 * Controller REST para endpoints de gestión de roles.
 *
 * Endpoints:
 * - GET    /api/roles              - Listar todos los roles
 * - GET    /api/roles/{id}         - Obtener rol por ID
 * - POST   /api/users/{userId}/roles/{roleId}   - Asignar rol a usuario
 * - DELETE /api/users/{userId}/roles/{roleId}   - Revocar rol de usuario
 *
 * Todos los endpoints requieren rol ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    private final GetAllRolesUseCase getAllRolesUseCase;
    private final GetRoleByIdUseCase getRoleByIdUseCase;
    private final AssignRoleToUserUseCase assignRoleToUserUseCase;
    private final RevokeRoleFromUserUseCase revokeRoleFromUserUseCase;

    public RoleController(
            GetAllRolesUseCase getAllRolesUseCase,
            GetRoleByIdUseCase getRoleByIdUseCase,
            AssignRoleToUserUseCase assignRoleToUserUseCase,
            RevokeRoleFromUserUseCase revokeRoleFromUserUseCase) {
        this.getAllRolesUseCase = getAllRolesUseCase;
        this.getRoleByIdUseCase = getRoleByIdUseCase;
        this.assignRoleToUserUseCase = assignRoleToUserUseCase;
        this.revokeRoleFromUserUseCase = revokeRoleFromUserUseCase;
    }

    /**
     * Obtiene todos los roles del sistema.
     *
     * Endpoint: GET /api/roles
     *
     * Requiere: Token JWT válido + rol ROLE_ADMIN
     *
     * Response exitosa (200 OK):
     * {
     *   "roles": [
     *     {
     *       "id": "uuid-1",
     *       "name": "ROLE_USER",
     *       "description": "Usuario estándar",
     *       "permissions": ["READ_PROFILE", "UPDATE_PROFILE"]
     *     },
     *     {
     *       "id": "uuid-2",
     *       "name": "ROLE_ADMIN",
     *       "description": "Administrador del sistema",
     *       "permissions": ["READ_USERS", "UPDATE_USERS", "DELETE_USERS"]
     *     }
     *   ],
     *   "total": 2
     * }
     */
    @GetMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoleListResponse> getAllRoles () {
        log.debug("Getting all roles");

        Set<Role> roles = getAllRolesUseCase.execute();

        RoleListResponse roleResponses = RoleListResponse.fromDomain(roles);

        return ResponseEntity.ok(roleResponses);
    }

    /**
     * Obtiene un rol por su ID.
     *
     * Endpoint: GET /api/roles/{id}
     *
     * Requiere: Token JWT válido + rol ROLE_ADMIN
     *
     * Response exitosa (200 OK):
     * {
     *   "id": "uuid-1",
     *   "name": "ROLE_USER",
     *   "description": "Usuario estándar",
     *   "permissions": ["READ_PROFILE", "UPDATE_PROFILE"]
     * }
     *
     * Errores:
     * - 404 Not Found: Rol no existe
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable UUID id) {
        log.debug("Getting role by ID: {}", id);

        Role role = getRoleByIdUseCase.execute(id);

        RoleResponse response = RoleResponse.fromDomain(role);

        return ResponseEntity.ok(response);
    }

    /**
     * Asigna un rol a un usuario.
     *
     * Endpoint: POST /api/users/{userId}/roles/{roleId}
     *
     * Requiere: Token JWT válido + rol ROLE_ADMIN
     *
     * Response exitosa (204 No Content)
     *
     * Errores:
     * - 404 Not Found: Usuario o rol no existe
     */
    @GetMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
     * Endpoint: DELETE /api/users/{userId}/roles/{roleId}
     *
     * Requiere: Token JWT válido + rol ROLE_ADMIN
     *
     * Response exitosa (204 No Content)
     *
     * Errores:
     * - 404 Not Found: Usuario o rol no existe
     */
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
