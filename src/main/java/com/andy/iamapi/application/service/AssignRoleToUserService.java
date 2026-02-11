package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.RoleNotFoundException;
import com.andy.iamapi.domain.exception.UserNotFoundException;
import com.andy.iamapi.domain.model.Role;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.AssignRoleToUserUseCase;
import com.andy.iamapi.domain.port.output.AuditLogger;
import com.andy.iamapi.domain.port.output.RoleRepository;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para asignar roles a usuarios.
 *
 * Flujo:
 * 1. Buscar usuario
 * 2. Buscar rol
 * 3. Asignar rol al usuario (domain method)
 * 4. Guardar usuario
 * 5. Registrar auditoría
 */
@Service
@Transactional
public class AssignRoleToUserService implements AssignRoleToUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(AssignRoleToUserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogger auditLogger;

    public AssignRoleToUserService (
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuditLogger auditLogger
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditLogger = auditLogger;
    }


    @Override
    public void execute (AssignRoleCommand command) {
        //PASO 1: Buscar usuario en la BD
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", command.userId());
                    return new UserNotFoundException(command.userId());
                });

        //PASO 2: Buscar el rol en la BD
        Role role = roleRepository.findById(command.roleId())
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", command.roleId());
                    return new RoleNotFoundException(command.roleId());
                });

        //PASO 3: Asignar rol (método del dominio)
        user.addRole(role);

        //PASO 4: Guarduar nuevo usuario
        userRepository.save(user);

        //PASO 5: Auditoría
        auditLogger.logAction(
                command.userId(),
                "ROLE_ASSIGNED",
                "ROLE:" + command.roleId(),
                "SYSTEM"
        );

        log.info("Role {} assigned successfully to user {}", role.getName(), command.userId());
    }
}
