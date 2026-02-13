package com.andy.iamapi.infrastructure.adapter.persistance.adapter;


import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.output.UserRepository;
import com.andy.iamapi.infrastructure.adapter.persistance.entity.UserEntity;
import com.andy.iamapi.infrastructure.adapter.persistance.mapper.UserMapper;
import com.andy.iamapi.infrastructure.adapter.persistance.repository.UserJpaRepository;
import com.andy.iamapi.infrastructure.adapter.persistance.specification.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * Busca a todos los usuarios
     *
     * @return List con todos los users
     */
    @Override
    public List<User> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Busca usuarios con paginación y filtros opcionales.
     *
     * Construye una Specification dinámica basada en los filtros proporcionados
     * y ejecuta la query con paginación.
     *
     * Flujo:
     * 1. Construir Specification con los filtros activos
     * 2. Ejecutar query paginada con JPA
     * 3. Mapear UserEntity → User (dominio)
     * 4. Retornar Page<User>
     *
     * @param email Filtro de email (opcional)
     * @param firstName Filtro de nombre (opcional)
     * @param lastName Filtro de apellido (opcional)
     * @param enabled Filtro de estado (opcional)
     * @param roleName Filtro de rol (opcional)
     * @param pageable Configuración de paginación
     * @return Page con usuarios del dominio
     */
    @Override
    public Page<User> findAllWithFilters(
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            String roleName,
            Pageable pageable
    ) {
        // 1. Construir la specification dinámica con los filtros
        Specification<UserEntity> spec = UserSpecifications.withFilters(
                email,
                firstName,
                lastName,
                enabled,
                roleName
        );

        // 2. Ejecutar query con JPA + Specification + Pageable
        Page<UserEntity> entityPage = jpaRepository.findAll(spec, pageable);

        // 3. Mapear Page<UserEntity> → Page<User>
        // Page.map() transforma el contenido pero mantiene la metadata de paginación
        return entityPage.map(mapper::toDomain);
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
     * Verifica si existe un usuario con el Id dado.
     *
     * Más eficiente que findById().isPresent() porque solo
     * ejecuta SELECT COUNT en vez de cargar toda la entidad.
     *
     * Query ejecutada:
     * {@code SELECT COUNT(*) FROM users WHERE id = ?}
     *
     * @param userId Id a verificar
     * @return true si existe al menos un usuario con ese id
     */
    @Override
    public boolean existsById(UUID userId) {
        return jpaRepository.existsById(userId);
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
