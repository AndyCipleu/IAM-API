package com.andy.iamapi.infrastructure.adapter.rest.controller;

import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.*;
import com.andy.iamapi.infrastructure.adapter.rest.dto.request.ChangePasswordRequest;
import com.andy.iamapi.infrastructure.adapter.rest.dto.request.UpdateUserRequest;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.UserListResponse;
import com.andy.iamapi.infrastructure.adapter.rest.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserController(
            GetCurrentUserUseCase getCurrentUserUseCase,
            GetAllUsersUseCase getAllUsersUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            UpdateUserUseCase updateUserUseCase,
            ChangePasswordUseCase changePasswordUseCase,
            DeleteUserUseCase deleteUserUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getAllUsersUseCase = getAllUsersUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    /**
     * Obtiene el perfil del usuario autenticado actualmente.
     *
     * Endpoint: GET /api/users/me
     *
     * Requiere: Token JWT válido en header Authorization
     *
     * Response exitosa (200 OK):
     * {
     *   "id": "uuid-123",
     *   "email": "john@example.com",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "enabled": true,
     *   "roles": ["ROLE_USER"],
     *   "createdAt": "2026-02-02T15:30:45"
     * }
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        log.debug("Getting current user profile: {}", currentUser.getId());

        User user = getCurrentUserUseCase.execute(currentUser.getId());

        UserResponse response = UserResponse.fromDomain(user);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los usuarios del sistema.
     *
     * Endpoint: GET /api/users
     *
     * Requiere: Token JWT válido + rol ROLE_ADMIN
     *
     * Response exitosa (200 OK):
     * {
     *   "users": [
     *     { "id": "...", "email": "...", ... },
     *     { "id": "...", "email": "...", ... }
     *   ],
     *   "total": 2
     * }
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserListResponse> getAllUsers() {
        log.debug("Getting all users");

        List<User> users = getAllUsersUseCase.execute();

        UserListResponse response = UserListResponse.fromDomain(users);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * Endpoint: GET /api/users/{id}
     *
     * Requiere: Token JWT válido + rol ROLE_ADMIN
     *
     * Response exitosa (200 OK):
     * {
     *   "id": "uuid-123",
     *   "email": "john@example.com",
     *   ...
     * }
     *
     * Errores:
     * - 404 Not Found: Usuario no existe
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.debug("Getting user by ID: {}", id);

        User user = getUserByIdUseCase.execute(id);

        UserResponse response = UserResponse.fromDomain(user);

        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza los datos de un usuario.
     *
     * Endpoint: PUT /api/users/{id}
     *
     * Requiere: Token JWT válido + (rol ROLE_ADMIN O ser el mismo usuario)
     *
     * Request body:
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "email": "john.doe@example.com"
     * }
     *
     * Response exitosa (200 OK):
     * {
     *   "id": "uuid-123",
     *   "email": "john.doe@example.com",
     *   ...
     * }
     *
     * Errores:
     * - 403 Forbidden: No tiene permiso para actualizar este usuario
     * - 404 Not Found: Usuario no existe
     * - 409 Conflict: Email ya existe
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.id")
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
     * Cambia la contraseña del usuario.
     *
     * Endpoint: PUT /api/users/{id}/password
     *
     * Requiere: Token JWT válido + ser el mismo usuario
     *
     * Request body:
     * {
     *   "currentPassword": "OldPass123",
     *   "newPassword": "NewPass456"
     * }
     *
     * Response exitosa (204 No Content)
     *
     * Errores:
     * - 401 Unauthorized: Contraseña actual incorrecta
     * - 400 Bad Request: Nueva contraseña no cumple requisitos
     * - 403 Forbidden: Solo puedes cambiar tu propia contraseña
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("#id == authentication.principal.id")
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
     * Endpoint: DELETE /api/users/{id}
     *
     * Requiere: Token JWT válido + rol ROLE_ADMIN
     *
     * Response exitosa (204 No Content)
     *
     * Errores:
     * - 404 Not Found: Usuario no existe
     * - 403 Forbidden: Solo admins pueden eliminar usuarios
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("Deleting user: {}", id);

        deleteUserUseCase.execute(id);

        log.info("User deleted successfully: {}", id);

        return ResponseEntity.noContent().build();
    }
}
