package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.GetAllUsersWithPaginationUseCase;
import com.andy.iamapi.domain.port.input.GetAllUsersWithPaginationUseCase.GetUsersCommand;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para obtener usuarios con paginación y filtros.
 *
 * Implementa el caso de uso de búsqueda paginada delegando
 * la lógica de persistencia al repositorio.
 *
 * Esta es la capa de aplicación (orquestación), no contiene lógica de negocio.
 */
@Service
@Transactional(readOnly = true)
public class GetAllUsersWithPaginationService implements GetAllUsersWithPaginationUseCase {
    private static final Logger log = LoggerFactory.getLogger(GetAllUsersWithPaginationService.class);

    private final UserRepository repository;

    public GetAllUsersWithPaginationService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Ejecuta la búsqueda paginada de usuarios.
     *
     * Delega la búsqueda al repositorio con los criterios proporcionados.
     *
     * @param command Comando con filtros y configuración de paginación
     * @return Page con los usuarios encontrados
     */
    @Override
    public Page<User> execute(GetUsersCommand command) {
        log.debug("Fetching users with pagination - page: {}, size: {}, filters: email={}, firstName={}, lastName={}, enabled={}, role={}",
                command.pageable().getPageNumber(),
                command.pageable().getPageSize(),
                command.email(),
                command.firstName(),
                command.lastName(),
                command.enabled(),
                command.roleName()
        );

        // Delegar al repositorio
        Page<User> users = repository.findAllWithFilters(
                command.email(),
                command.firstName(),
                command.lastName(),
                command.enabled(),
                command.roleName(),
                command.pageable()
        );

        log.debug("Found {} users in page {} of {}",
                users.getNumberOfElements(),
                users.getNumber(),
                users.getTotalPages()
        );

        return users;
    }
}
