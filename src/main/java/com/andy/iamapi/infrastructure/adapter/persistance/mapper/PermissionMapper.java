package com.andy.iamapi.infrastructure.adapter.persistance.mapper;

import com.andy.iamapi.domain.model.Permission;
import com.andy.iamapi.infrastructure.adapter.persistance.entity.PermissionEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Permission (dominio) y PermissionEntity (JPA).
 *
 * Responsabilidad única: traducción entre capas.
 * No contiene lógica de negocio.
 *
 * @see Permission
 * @see PermissionEntity
 */
@Component
public class PermissionMapper {

    /**
     * Convierte una entidad JPA a modelo de dominio.
     *
     * Esta conversión es stateless (sin estado) y pura:
     * misma entity siempre produce mismo domain model.
     *
     * Uso típico:
     * Después de leer de BD, convertir a domain para pasarlo a los services.
     *
     * @param entity Entidad JPA (puede ser null)
     * @return Modelo de dominio, o null si entity es null
     */
    public Permission toDomain(PermissionEntity entity) {
        if (entity == null) return null;

        Permission permission = Permission.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getResource(),
                entity.getAction(),
                entity.getDescription()
        );

        return permission;
    }

    public PermissionEntity toEntity(Permission permission) {
        if (permission == null) return null;

        return PermissionEntity.builder()
                .id(permission.getId())
                .name(permission.getName())
                .action(permission.getAction())
                .resource(permission.getResource())
                .description(permission.getDescription())
                .build();
    }
}
