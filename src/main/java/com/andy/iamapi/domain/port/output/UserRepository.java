package com.andy.iamapi.domain.port.output;


import com.andy.iamapi.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de salida para persistencia de usuarios
 *
 * PRINCIPIOS APLICADOS:
 * - Interface Segregation: Solo métodos necesarios para User
 * - Dependency Inversion: El dominio define el contrato, no la infraestructura
 */
public interface UserRepository {

    /**
     * Persiste un usuario nuevo o actualiza uno existente
     * @param user Usuario a persistir
     * @return Usuario persistido con datos actualizados (ej: timestamps)
     */
    User save(User user);

    /**
     * Busca un usuario por su email único
     * @param email Email del usuario (case-insensitive)
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su ID
     * @param id UUID del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findById(UUID id);

    /**
     * Busca a todos los usuarios
     * @return Listm lista con todos los usuarios
     */
    List<User> findAll();

    /**
     * Busca usuarios con paginación y filtros opcionales.
     *
     * Permite buscar usuarios aplicando múltiples criterios de filtrado
     * y retornar resultados paginados.
     *
     * @param email Filtro de email (opcional, búsqueda parcial)
     * @param firstName Filtro de nombre (opcional, búsqueda parcial)
     * @param lastName Filtro de apellido (opcional, búsqueda parcial)
     * @param enabled Filtro de estado habilitado (opcional)
     * @param roleName Filtro por nombre de rol (opcional)
     * @param pageable Configuración de paginación (página, tamaño, ordenamiento)
     * @return Page con los usuarios que cumplen los criterios
     */
    Page<User> findAllWithFilters(
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            String roleName,
            Pageable pageable
    );
    /**
     * Verifica si existe un usuario con el email dado
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el id dado
     * @param userId Id a verificar
     * @return true si existe, false si no
     */
    boolean existsById(UUID userId);

    /**
     * Elimina un usuario (soft delete recomendado)
     * @param id UUID del usuario a eliminar
     */
    void deleteById(UUID id);
}
