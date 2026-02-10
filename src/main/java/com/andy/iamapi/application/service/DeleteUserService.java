package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.UserNotFoundException;
import com.andy.iamapi.domain.port.input.DeleteUserUseCase;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio para eliminar usuarios.
 */
@Service
@Transactional
public class DeleteUserService implements DeleteUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(DeleteUserService.class);

    private final UserRepository repository;

    public DeleteUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(UUID userId) {
        log.info("Deleting user: {}", userId);

        // Verificar que el usuario existe
        if (!repository.existsById(userId)) {
            log.warn("User not found: {}", userId);
            throw new UserNotFoundException(userId);
        }

        //Eliminar usuario
        repository.deleteById(userId);

        log.info("User deleted successfully: {}", userId);
    }
}
