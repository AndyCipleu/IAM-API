package com.andy.iamapi.infrastructure.adapter.persistance.specification;

import com.andy.iamapi.infrastructure.adapter.persistance.entity.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para construir queries dinámicas de UserEntity.
 *
 * Cada método estático retorna una Specification que representa
 * un criterio de búsqueda. Las specifications se pueden combinar
 * con operadores lógicos (AND, OR).
 *
 * Patrón Specification de Domain-Driven Design aplicado con JPA Criteria API.
 *
 * Ejemplo de uso:
 * <pre>
 * Specification<UserEntity> spec = Specification
 *     .where(hasEmail("john"))
 *     .and(hasFirstName("John"))
 *     .and(hasRole("ROLE_ADMIN"));
 * </pre>
 */
public class UserSpecifications {
    /**
     * Specification para filtrar por email.
     *
     * Usa búsqueda parcial (LIKE) y case-insensitive (LOWER).
     * Si email es null, no aplica ningún filtro (retorna null).
     *
     * SQL generado (ejemplo):
     * WHERE LOWER(email) LIKE LOWER('%john%')
     *
     * @param email Texto a buscar en el email (puede ser null)
     * @return Specification que filtra por email, o null si email es null
     */
    public static Specification<UserEntity> hasEmail(String email) {
        // Si el parámetro es null, no aplicar filtro
        return ((root, query, criteriaBuilder) -> {
           if (email == null || email.isBlank()) {
               return null; // No aplica filtro
           }

           //LOWER(email) LIKE LOWER('%john%')
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%"
            );
        });
    }

    /**
     * Specification para filtrar por firstName.
     *
     * Usa búsqueda parcial (LIKE) y case-insensitive (LOWER).
     *
     * SQL generado (ejemplo):
     * WHERE LOWER(first_name) LIKE LOWER('%john%')
     *
     * @param firstName Texto a buscar en el nombre (puede ser null)
     * @return Specification que filtra por firstName, o null si firstName es null
     */
    public static Specification<UserEntity> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) -> {
            if (firstName == null || firstName.isBlank()) {
                return null;
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")),
                    "%" + firstName.toLowerCase() + "%"
            );
        };
    }

    /**
     * Specification para filtrar por lastName.
     *
     * Usa búsqueda parcial (LIKE) y case-insensitive (LOWER).
     *
     * SQL generado (ejemplo):
     * WHERE LOWER(last_name) LIKE LOWER('%doe%')
     *
     * @param lastName Texto a buscar en el apellido (puede ser null)
     * @return Specification que filtra por lastName, o null si lastName es null
     */
    public static Specification<UserEntity> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) -> {
            if (lastName == null || lastName.isBlank()) {
                return null;
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")),
                    "%" + lastName.toLowerCase() + "%"
            );
        };
    }

    /**
     * Specification para filtrar por estado habilitado/deshabilitado.
     *
     * SQL generado (ejemplo):
     * WHERE enabled = true
     *
     * @param enabled Estado deseado (puede ser null)
     * @return Specification que filtra por enabled, o null si enabled es null
     */
    public static Specification<UserEntity> isEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) {
                return null;
            }

            // WHERE enabled = true/false
            return criteriaBuilder.equal(root.get("enabled"), enabled);
        };
    }

    /**
     * Specification para filtrar por rol.
     *
     * Realiza un JOIN con la tabla de roles para filtrar usuarios
     * que tengan un rol específico.
     *
     * SQL generado (ejemplo):
     * JOIN user_roles ur ON u.id = ur.user_id
     * JOIN roles r ON ur.role_id = r.id
     * WHERE r.name = 'ROLE_ADMIN'
     *
     * @param roleName Nombre del rol (ej: "ROLE_ADMIN", puede ser null)
     * @return Specification que filtra por rol, o null si roleName es null
     */
    public static Specification<UserEntity> hasRole(String roleName) {
        return (root, query, criteriaBuilder) -> {
            if (roleName == null || roleName.isBlank()) {
                return null;
            }

            // JOIN con la tabla roles
            // root = UserEntity
            // root.join("roles") = Join<UserEntity, RoleEntity>
            Join<Object, Object> rolesJoin = root.join("roles", JoinType.INNER);

            // WHERE role.name = 'ROLE_ADMIN'
            return criteriaBuilder.equal(rolesJoin.get("name"), roleName);
        };
    }

    /**
     * Combina múltiples specifications con operador AND.
     *
     * Helper method para construir una specification completa
     * combinando todos los filtros opcionales.
     *
     * Solo añade specifications que no sean null (filtros activos).
     *
     * @param email Filtro de email (opcional)
     * @param firstName Filtro de nombre (opcional)
     * @param lastName Filtro de apellido (opcional)
     * @param enabled Filtro de estado (opcional)
     * @param roleName Filtro de rol (opcional)
     * @return Specification combinada con todos los filtros activos
     */
    public static Specification<UserEntity> withFilters(
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            String roleName
    ) {

        // Specification.where() crea una specification inicial (siempre true)
        // Luego vamos añadiendo filtros con .and()
        // Si una specification es null, simplemente no se añade

        return Specification
                .where(hasEmail(email))           // AND email LIKE ...
                .and(hasFirstName(firstName))     // AND firstName LIKE ...
                .and(hasLastName(lastName))       // AND lastName LIKE ...
                .and(isEnabled(enabled))          // AND enabled = ...
                .and(hasRole(roleName));          // AND role.name = ...
    }
}
