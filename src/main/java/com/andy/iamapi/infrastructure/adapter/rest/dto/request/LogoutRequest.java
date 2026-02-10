package com.andy.iamapi.infrastructure.adapter.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitud de logout.
 */
public record LogoutRequest(
        @NotBlank(message = "Access token is required")
        String accessToken,
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
