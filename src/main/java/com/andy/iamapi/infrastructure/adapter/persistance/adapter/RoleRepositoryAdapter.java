package com.andy.iamapi.infrastructure.adapter.persistance.adapter;

import com.andy.iamapi.domain.model.Role;
import com.andy.iamapi.domain.port.output.RoleRepository;
import com.andy.iamapi.infrastructure.adapter.persistance.entity.RoleEntity;
import com.andy.iamapi.infrastructure.adapter.persistance.mapper.RoleMapper;
import com.andy.iamapi.infrastructure.adapter.persistance.repository.RoleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter que implementa el port RoleRepository usando JPA.
 *
 * @see RoleRepository
 * @see RoleJpaRepository
 * @see RoleMapper
 */
@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository jpaRepository;
    private final RoleMapper mapper;

    public RoleRepositoryAdapter(RoleJpaRepository jpaRepository, RoleMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Role save(Role role) {
        return mapper.toDomain(
                jpaRepository.save(
                        mapper.roleEntity(role)
                )
        );
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Set<Role> findByNameIn(Set<String> names) {
        return jpaRepository.findByNameIn(names).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());

    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public Set<Role> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

