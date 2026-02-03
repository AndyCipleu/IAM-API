package com.andy.iamapi.infrastructure.adapter.persistance.mapper;

import com.andy.iamapi.domain.model.Role;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.infrastructure.adapter.persistance.entity.RoleEntity;
import com.andy.iamapi.infrastructure.adapter.persistance.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * Mapper para convertir entre User (dominio) y UserEntity (JPA).
 *
 * Depende de RoleMapper para convertir los roles asociados.
 */
@Component
public class UserMapper {
    private RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;

        User user = User.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEnabled(),
                entity.getAccountNonLocked(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );

        if(entity.getRoles() != null) {
            Set<Role> roles = entity.getRoles().stream()
                    .map(roleMapper::toDomainWithoutPermissions)
                    .collect(Collectors.toSet());

            roles.forEach(user::addRole);
        }

        return user;
    }

    public User toDomainWithRolesAndPermissions (UserEntity entity) {
        if (entity == null) return null;

        User user = User.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEnabled(),
                entity.getAccountNonLocked(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );

        if(entity.getRoles() != null) {
            Set<Role> roles = entity.getRoles().stream()
                    .map(roleMapper::toDomain)
                    .collect(Collectors.toSet());

            roles.forEach(user::addRole);
        }

        return user;
    }

    /**
     * Convierte una entidad JPA a modelo de dominio SIN roles.
     *
     * Útil cuando solo necesitas datos básicos del usuario.
     *
     * @param entity Entidad JPA
     * @return Modelo de dominio sin roles
     */
    public User toDomainWithoutRoles (UserEntity entity) {
        if (entity == null) return null;

        return
                User.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEnabled(),
                entity.getAccountNonLocked(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public UserEntity toEntity(User user) {
        if (user == null) return null;

        UserEntity entity = UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        if (user.getRoles() != null) {
            Set<RoleEntity> rolesEntities = user.getRoles().stream()
                    .map(roleMapper::roleEntity)
                    .collect(Collectors.toSet());

            entity.setRoles(rolesEntities);
        }

        return entity;
    }
}
