package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.UserAlreadyExistsException;
import com.andy.iamapi.domain.exception.UserNotFoundException;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.UpdateUserUseCase;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para actualizar usuarios.
 */
@Service
@Transactional
public class UpdateUserService implements UpdateUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(UpdateUserService.class);

    private final UserRepository repository;

    public UpdateUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(UpdateUserCommand command) {
        log.info("Updating user: {}", command.userId());

        //Buscar usuario
        User user = repository.findById(command.userId())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", command.userId());
                    throw new UserNotFoundException(command.userId());
                });

        //Si cambió de email, verificar que dicho email no exista en la BD
        if (!user.getEmail().equals(command.email())) {
            if (repository.existsByEmail(command.email())) {
                log.warn("Email already exists: {}", command.email());
                throw new UserAlreadyExistsException(command.email());
            }
        }

        user = User.reconstitute(
                user.getId(),
                command.email(),
                user.getPassword(),
                command.firstName(),
                command.lastName(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.getCreatedAt(),
                LocalDateTime.now()
        );

        //Guardar
        User savedUser = repository.save(user);

        log.info("User updated successfully: {}", savedUser.getId());

        return savedUser;
    }
}
