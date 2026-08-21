package org.devbot.bookmymovie.identity.api.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserResponse user
) {}
