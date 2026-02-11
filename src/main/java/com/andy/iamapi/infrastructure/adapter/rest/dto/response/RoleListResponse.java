package com.andy.iamapi.infrastructure.adapter.rest.dto.response;

import com.andy.iamapi.domain.model.Role;

import java.util.Set;
import java.util.stream.Collectors;

public record RoleListResponse(
        Set<RoleResponse> roles,
        int total
) {
    /**
     * Convierte Set de Role (domain) a RoleListResponse (DTO).
     *
     * @param roles Set de roles del dominio
     * @return RoleListResponse
     */
    public static RoleListResponse fromDomain(Set<Role> roles) {
        Set<RoleResponse> roleResponses = roles
                .stream()
                .map(RoleResponse::fromDomain)
                .collect(Collectors.toSet());

        return new RoleListResponse(
                roleResponses,
                roleResponses.size()
        );

    }
}
