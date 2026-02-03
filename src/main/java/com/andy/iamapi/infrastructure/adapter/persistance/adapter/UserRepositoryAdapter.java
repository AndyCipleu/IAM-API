package com.andy.iamapi.infrastructure.adapter.persistance.adapter;


import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.output.UserRepository;
import com.andy.iamapi.infrastructure.adapter.persistance.entity.UserEntity;
import com.andy.iamapi.infrastructure.adapter.persistance.mapper.UserMapper;
import com.andy.iamapi.infrastructure.adapter.persistance.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa el port UserRepository usando JPA.
 *
 * Este adapter es el puente entre el dominio (port UserRepository)
 * y la infraestructura (JPA).
 *
 * Responsabilidades:
 * - Implementar los métodos del port UserRepository
 * - Delegar operaciones a UserJpaRepository (Spring Data)
 * - Convertir entre User (domain) y UserEntity (JPA) usando UserMapper
 *
 * SOLID aplicado:
 * - Single Responsibility: Solo adapta el repositorio JPA al port del dominio
 * - Dependency Inversion: Implementa una abstracción (port) definida por el dominio
 * - Open/Closed: Extensible (puedes agregar otro adapter sin tocar este)
 *
 * @see UserRepository
 * @see UserJpaRepository
 * @see UserMapper
 */
@Component
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public UserRepositoryAdapter (UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * Guarda un usuario en la base de datos.
     *
     * Flujo:
     * 1. Convierte User (domain) a UserEntity (JPA)
     * 2. Guarda con JPA (inserta o actualiza según si existe el ID)
     * 3. Convierte UserEntity guardado de vuelta a User
     * 4. Retorna User con datos actualizados (timestamps, etc)
     *
     * Nota: JPA automáticamente detecta si es INSERT o UPDATE:
     * - Si entity.id es null o no existe en BD → INSERT
     * - Si entity.id existe en BD → UPDATE
     *
     * @param user Usuario de dominio a persistir
     * @return Usuario guardado con datos actualizados
     */
    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);

        UserEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    /**
     * Busca un usuario por email.
     *
     * Flujo:
     * 1. Busca UserEntity por email con JPA
     * 2. Si existe, convierte a User (domain)
     * 3. Retorna Optional
     *
     * Nota: Esta query NO carga roles (LAZY).
     * Si necesitas roles, usa findByEmailWithRoles en el JpaRepository.
     *
     * @param email Email del usuario (case-sensitive)
     * @return Optional con User si existe, Optional.empty() si no
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                        .map(mapper::toDomain);
    }

    /**
     * Busca un usuario por ID.
     *
     * @param id UUID del usuario
     * @return Optional con User si existe
     */
    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * Más eficiente que findByEmail().isPresent() porque solo
     * ejecuta SELECT COUNT en vez de cargar toda la entidad.
     *
     * Query ejecutada:
     * {@code SELECT COUNT(*) FROM users WHERE email = ?}
     *
     * @param email Email a verificar
     * @return true si existe al menos un usuario con ese email
     */
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }


    /**
     * Elimina un usuario por ID.
     *
     * Nota: En sistemas reales, considera usar soft delete en vez de
     * delete físico (agregar campo deleted_at y filtrarlo en queries).
     *
     * Soft delete preserva:
     * - Auditoría (quién existió alguna vez)
     * - Relaciones históricas
     * - Posibilidad de restaurar
     *
     * @param id UUID del usuario a eliminar
     */
    @Override
    public void deleteById(UUID id){
        jpaRepository.deleteById(id);
    }
}
