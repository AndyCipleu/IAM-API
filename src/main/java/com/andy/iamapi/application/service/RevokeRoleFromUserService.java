package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.RoleNotFoundException;
import com.andy.iamapi.domain.exception.UserNotFoundException;
import com.andy.iamapi.domain.model.Role;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.RevokeRoleFromUserUseCase;
import com.andy.iamapi.domain.port.input.RevokeRoleFromUserUseCase.RevokeRoleCommand;
import com.andy.iamapi.domain.port.output.AuditLogger;
import com.andy.iamapi.domain.port.output.RoleRepository;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para revocar roles de usuarios.
 *
 * Flujo:
 * 1. Buscar usuario
 * 2. Buscar rol
 * 3. Revocar rol del usuario (domain method)
 * 4. Guardar usuario
 * 5. Registrar auditoría
 */
@Service
@Transactional
public class RevokeRoleFromUserService implements RevokeRoleFromUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(RevokeRoleFromUserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogger auditLogger;

    public RevokeRoleFromUserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditLogger = auditLogger;
    }

    @Override
    public void execute(RevokeRoleCommand command) {
        log.info("Revoking role {} from user {}", command.roleId(), command.userId());

        //PASO 1: Buscar usuario
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", command.userId());
                    return new UserNotFoundException(command.userId());
                });

        //PASO 1: Buscar rol
        Role role = roleRepository.findById(command.roleId())
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", command.roleId());
                    return new RoleNotFoundException(command.roleId());
                });

        //PASO 3: Revocar rol (método del dominio)
        user.removeRole(role);

        //PASO 4: Guardar usuario en BD
        userRepository.save(user);

        //PASO 5: Autidotía
        auditLogger.logAction(
                command.userId(),
                "ROLE_REVOKED",
                "ROLE:" + command.roleId(),
                "SYSTEM"
        );

        log.info("Role {} revoked successfully from user {}", role.getName(), command.userId());
    }
}
