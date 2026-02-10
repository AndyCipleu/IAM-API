package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.InvalidCredentialsException;
import com.andy.iamapi.domain.exception.InvalidPasswordException;
import com.andy.iamapi.domain.exception.UserNotFoundException;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.ChangePasswordUseCase;
import com.andy.iamapi.domain.port.output.PasswordEncoder;
import com.andy.iamapi.domain.port.output.UserRepository;
import com.andy.iamapi.domain.util.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para cambiar contraseña de usuario.
 *
 * Flujo:
 * 1. Buscar usuario
 * 2. Verificar contraseña actual con BCrypt
 * 3. Validar nueva contraseña (requisitos de seguridad)
 * 4. Encriptar nueva contraseña
 * 5. Crear nuevo User con contraseña actualizada (reconstitute)
 * 6. Guardar
 */
@Service
@Transactional
public class ChangePasswordService implements ChangePasswordUseCase {
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private static final Logger log = LoggerFactory.getLogger(ChangePasswordService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordService(
            UserRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void execute(ChangePasswordCommand command) {
        log.info("Changing password for user: {}", command.userId());

        //PASO 1: Buscar usuario
        User user = repository.findById(command.userId())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", command.userId());
                    return new UserNotFoundException(command.userId());
                });

        //PASO 2: Verificar contraseña actual
        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            log.warn("Invalid current password for user: {}", command.userId());
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        //PASO 3: Validar nueva contraseña (requisitos de seguridad)
        PasswordValidator.validate(command.newPassword());

        //PASO 4: Encriptar la nueva contraseña
        String encodedPassword = passwordEncoder.encode(command.currentPassword());

        //PASO 5: Crear nuevo User con la contraseña actualizada(reconstite)
        User updatedUser = User.reconstitute(
                user.getId(),
                user.getEmail(),
                encodedPassword,
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.getCreatedAt(),
                LocalDateTime.now()
        );

        repository.save(updatedUser);

        log.info("Password changed successfully for user: {}", command.userId());
    }

}
