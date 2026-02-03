package com.andy.iamapi.domain.port.output;

import com.andy.iamapi.domain.model.Permission;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Port de salida para persistencia de permisos
 */
public interface PermissionRepository {
    /**
     * Persiste un permiso nuevo o actualiza uno existente
     * @param permission Permiso a persistir
     * @return Permiso persistido con datos actualizados
     */
    Permission save(Permission permission);

    /**
     * Busca un permiso por su nombre
     * @param name Nombre del permiso
     * @return Optional con el permiso si existe
     */
    Optional<Permission> findByName(String name);

    /**
     * Busca un permiso por su ID
     * @param id UUID del permiso
     * @return Optional con el permiso si existe
     */
    Optional<Permission> findById(UUID id);

    /**
     * Busca permisos por recurso y acción
     * Ej: findByResourceAndAction("USER", "READ")
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Busca múltiples permisos por sus nombres
     * Útil para asignación masiva de permisos
     */
    Set<Permission> findByNameIn(Set<String> names);

    /**
     * Verifica si existe un permiso con el nombre dado
     * @param name Nombre a verificar
     * @return true si existe, false si no
     */
    boolean existsByName(String name);

    /**
     * Obtiene todos los permisos disponibles
     * @return Set de todos los permisos del sistema
     */
    Set<Permission> findAll();

    void deleteById(UUID id);
}
