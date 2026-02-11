package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.RoleNotFoundException;
import com.andy.iamapi.domain.model.Role;
import com.andy.iamapi.domain.port.input.GetRoleByIdUseCase;
import com.andy.iamapi.domain.port.output.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio para obtener un rol por ID.
 */
@Service
@Transactional(readOnly = true)
public class GetRoleByIdService implements GetRoleByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetRoleByIdService.class);

    private final RoleRepository roleRepository;

    public GetRoleByIdService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role execute(UUID roleId) {
        log.debug("Getting role by ID: {}", roleId);

        return roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", roleId);
                    return new RoleNotFoundException(roleId);
                });
    }
}
