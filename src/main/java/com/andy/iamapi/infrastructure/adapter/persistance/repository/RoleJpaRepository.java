package com.andy.iamapi.infrastructure.adapter.persistance.repository;

import com.andy.iamapi.infrastructure.adapter.persistance.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad RoleEntity.
 *
 * @see RoleEntity
 */
@Repository
public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);

    Set<RoleEntity> findByNameIn(Set<String> names);

    @Query("SELECT r FROM RoleEntity r LEFT JOIN FETCH r.permissions WHERE r.name =: name")
    Optional<RoleEntity> findByNameWithPermissions(@Param("name") String name);



}
