package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.GetAllUsersUseCase;
import com.andy.iamapi.domain.port.output.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para obtener todos los usuarios.
 */
@Service
@Transactional(readOnly = true)
public class GetAllUsersService implements GetAllUsersUseCase {
    private static final Logger log = LoggerFactory.getLogger(GetAllUsersService.class);

    private final UserRepository repository;

    public GetAllUsersService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<User> execute() {
        log.debug("Getting all users");
        return repository.findAll();
    }
}
