package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.UserNotFoundException;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.GetUserByIdUseCase;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio para obtener un usuario por ID.
 */
@Service
@Transactional(readOnly = true)
public class GetUserByIdService implements GetUserByIdUseCase {
    private static final Logger log = LoggerFactory.getLogger(GetUserByIdService.class);

    private final UserRepository repository;

    public GetUserByIdService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(UUID userId) {
        log.debug("Getting user with ID: {}", userId);

        return repository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new UserNotFoundException(userId);
                });
    }
}
