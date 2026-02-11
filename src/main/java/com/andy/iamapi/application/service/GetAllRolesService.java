package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.model.Role;
import com.andy.iamapi.domain.port.input.GetAllRolesUseCase;
import com.andy.iamapi.domain.port.output.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para obtener todos los roles.
 */
@Service
@Transactional(readOnly = true)
public class GetAllRolesService implements GetAllRolesUseCase {
    private static final Logger log = LoggerFactory.getLogger(GetAllRolesService.class);

    private final RoleRepository roleRepository;

    public GetAllRolesService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Set<Role> execute() {
        log.debug("Getting all roles");
        return roleRepository.findAll();
    }
}
