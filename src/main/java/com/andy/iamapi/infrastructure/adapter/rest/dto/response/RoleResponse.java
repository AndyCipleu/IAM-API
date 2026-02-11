package com.andy.iamapi.infrastructure.adapter.rest.dto.response;

import com.andy.iamapi.domain.model.Permission;
import com.andy.iamapi.domain.model.Role;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record RoleResponse(
        UUID roleId,
        String name,
        String description,
        Set<String> permisos
) {
    /**
     * Convierte Role (domain) a RoleResponse (DTO).
     *
     * @param role Rol del dominio
     * @return RoleResponse
     */
    public static RoleResponse fromDomain(Role role) {
        Set<String> permissionNames = role.getPermissions()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissionNames
        );
    }
}
