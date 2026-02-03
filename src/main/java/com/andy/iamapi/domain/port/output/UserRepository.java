package com.andy.iamapi.domain.port.output;


import com.andy.iamapi.domain.model.User;

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
     * Verifica si existe un usuario con el email dado
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Elimina un usuario (soft delete recomendado)
     * @param id UUID del usuario a eliminar
     */
    void deleteById(UUID id);
}
