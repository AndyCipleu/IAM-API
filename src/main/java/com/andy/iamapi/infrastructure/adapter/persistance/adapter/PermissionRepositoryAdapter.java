package com.andy.iamapi.infrastructure.adapter.persistance.adapter;

import com.andy.iamapi.domain.model.Permission;
import com.andy.iamapi.domain.port.output.PermissionRepository;
import com.andy.iamapi.infrastructure.adapter.persistance.mapper.PermissionMapper;
import com.andy.iamapi.infrastructure.adapter.persistance.repository.PermissionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter que implementa el port PermissionRepository usando JPA.
 *
 * @see PermissionRepository
 * @see PermissionJpaRepository
 * @see PermissionMapper
 */
@Component
public class PermissionRepositoryAdapter implements PermissionRepository {
    private final PermissionJpaRepository jpaRepository;
    private final PermissionMapper mapper;

    public PermissionRepositoryAdapter(PermissionJpaRepository jpaRepository, PermissionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Permission save(Permission permission) {
        return mapper.toDomain(
                jpaRepository.save(
                        mapper.toEntity(permission)
                )
        );
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Permission> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Permission> findByResourceAndAction(String resource, String action) {
        return jpaRepository.findByResourceAndAction(resource, action)
                .map(mapper::toDomain);
    }

    @Override
    public Set<Permission> findByNameIn(Set<String> names) {
        return jpaRepository.findByNameIn(names)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public Set<Permission> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
