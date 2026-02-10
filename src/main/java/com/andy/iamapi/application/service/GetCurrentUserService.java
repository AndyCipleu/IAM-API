package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.UserNotFoundException;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.GetCurrentUserUseCase;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio para obtener el usuario actual (me).
 */
@Service
@Transactional(readOnly = true)
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetCurrentUserService.class);
    private final UserRepository repository;


    public GetCurrentUserService (UserRepository repository) {
        this.repository = repository;
    }


    @Override
    public User execute(UUID userId) {
        log.debug("Getting current user with ID: {}", userId);

        return repository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new UserNotFoundException(userId);
                });

    }
}
