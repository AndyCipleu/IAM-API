package com.andy.iamapi.infrastructure.adapter.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitud de refresh token.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
