package com.andy.iamapi.infrastructure.adapter.persistance.repository;

import com.andy.iamapi.infrastructure.adapter.persistance.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.naming.Name;
import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad PermissionEntity.
 *
 * @see PermissionEntity
 */
@Repository
public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, UUID> {

    Optional<PermissionEntity> findByName(String name);

    boolean existsByName(String name);

    Optional<PermissionEntity> findByResourceAndAction(String resource, String action);

    Set<PermissionEntity> findByNameIn(Set<String> names);

    Set<PermissionEntity> findByResource(String resource);

}
