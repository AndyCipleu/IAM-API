package com.andy.iamapi.infrastructure.adapter.rest.dto.response;

import com.andy.iamapi.domain.model.User;

import java.util.List;
import java.util.stream.Collectors;

public record UserListResponse(
        List<UserResponse> users,
        int total
) {
    /**
     * Convierte una lista de User (domain) a UserListResponse (DTO).
     *
     * @param users Lista de usuarios del dominio
     * @return UserListResponse
     */
    public static UserListResponse fromDomain(List<User> users) {
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::fromDomain)
                .collect(Collectors.toList());

        return new UserListResponse(
                userResponses,
                userResponses.size()
        );
    }
}
