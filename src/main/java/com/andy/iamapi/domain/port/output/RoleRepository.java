package com.andy.iamapi.domain.port.output;

import com.andy.iamapi.domain.model.Role;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Port de salida para persistencia de roles
 */
public interface RoleRepository {
    /**
     * Persiste un rol nuevo o actualiza uno existente
     * @param role Rol a persistir
     * @return Rol persistido con datos actualizados
     */
    Role save(Role role);

    /**
     * Busca un rol por su nombre
     * @param name Nombre del rol
     * @return Optional con el rol si existe
     */
    Optional<Role> findByName(String name);

    /**
     * Busca un rol por su ID
     * @param id UUID del rol
     * @return Optional con el rol si existe
     */
    Optional<Role> findById(UUID id);

    /**
     * Busca múltiples roles por sus nombres
     * Útil para asignación masiva de roles
     */
    Set<Role> findByNameIn(Set<String> names);

    /**
     * Verifica si existe un rol con el nombre dado
     * @param name Nombre a verificar
     * @return true si existe, false si no
     */
    boolean existsByName(String name);

    /**
     * Obtiene todos los roles disponibles
     * @return Set de todos los roles del sistema
     */
    Set<Role> findAll();

    void deleteById(UUID id);
}
