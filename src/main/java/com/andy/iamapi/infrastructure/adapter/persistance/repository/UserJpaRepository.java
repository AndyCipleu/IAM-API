package com.andy.iamapi.infrastructure.adapter.persistance.repository;

import com.andy.iamapi.infrastructure.adapter.persistance.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad UserEntity.
 *
 * Spring Data JPA genera automáticamente la implementación de este repositorio
 * basándose en los nombres de los métodos (Query Methods).
 *
 * Convención de nombres:
 * - findBy{Campo} → SELECT WHERE campo = ?
 * - existsBy{Campo} → SELECT COUNT WHERE campo = ?
 * - deleteBy{Campo} → DELETE WHERE campo = ?
 *
 * @see UserEntity
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface UserJpaRepository extends
        JpaRepository<UserEntity, UUID>,
        JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles WHERE u.email =: email")
    Optional<UserEntity> findByEmailWithRoles(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles WHERE u.id =: id")
    Optional<UserEntity> findByIdWithRoles(@Param("id") UUID id);
}
