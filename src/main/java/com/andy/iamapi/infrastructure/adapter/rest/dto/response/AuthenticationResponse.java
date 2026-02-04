package com.andy.iamapi.infrastructure.adapter.rest.dto.response;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public AuthenticationResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserResponse user
    ) {
        this(accessToken,refreshToken,"Bearer",expiresIn,user);
    }
}
