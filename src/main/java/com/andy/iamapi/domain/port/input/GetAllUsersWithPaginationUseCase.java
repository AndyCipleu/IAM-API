package com.andy.iamapi.domain.port.input;

import com.andy.iamapi.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Puerto de entrada (Use Case) para obtener usuarios con paginación y filtros.
 *
 * Caso de uso: Como administrador, quiero listar usuarios con paginación
 * y aplicar filtros opcionales para encontrar usuarios específicos.
 */
public interface GetAllUsersWithPaginationUseCase {
    /**
     * Ejecuta la búsqueda paginada de usuarios.
     *
     * @param command Comando con los criterios de búsqueda
     * @return Page con los usuarios que cumplen los criterios
     */
    Page<User> execute (GetUsersCommand command);

    /**
     * Comando para buscar usuarios con filtros opcionales.
     *
     * Encapsula todos los parámetros de búsqueda en un objeto inmutable.
     *
     * @param email Filtro de email (opcional)
     * @param firstName Filtro de nombre (opcional)
     * @param lastName Filtro de apellido (opcional)
     * @param enabled Filtro de estado (opcional)
     * @param roleName Filtro de rol (opcional)
     * @param pageable Configuración de paginación
     */
    public record GetUsersCommand(
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            String roleName,
            Pageable pageable
    ) {}
}
