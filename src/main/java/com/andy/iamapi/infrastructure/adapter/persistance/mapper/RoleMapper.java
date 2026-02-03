package com.andy.iamapi.infrastructure.adapter.persistance.mapper;

import com.andy.iamapi.domain.model.Permission;
import com.andy.iamapi.domain.model.Role;
import com.andy.iamapi.infrastructure.adapter.persistance.entity.PermissionEntity;
import com.andy.iamapi.infrastructure.adapter.persistance.entity.RoleEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Role (dominio) y RoleEntity (JPA).
 *
 * Depende de PermissionMapper para convertir los permisos asociados.
 */
@Component
public class RoleMapper {
    private PermissionMapper permissionMapper;

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }
    public Role toDomain(RoleEntity entity) {
        if (entity == null) return null;

        Role role = Role.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );

        if (entity.getPermissions() != null) {
            Set<Permission> permissions = entity.getPermissions().stream()
                    .map(permissionMapper::toDomain)
                    .collect(Collectors.toSet());

            permissions.forEach(role::addPermission);
        }

        return role;
    }

    public RoleEntity roleEntity (Role role) {
        if (role == null) return null;

        RoleEntity entity = RoleEntity.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();

        if (role.getPermission() != null) {
            Set<PermissionEntity> permissionsEntities = role.getPermissions().stream()
                    .map(permissionMapper::toEntity)
                    .collect(Collectors.toSet());

            entity.setPermissions(permissionsEntities);
        }

        return entity;
    }

    public Role toDomainWithoutPermissions(RoleEntity entity) {
        if (entity == null) return null;

        return Role.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
