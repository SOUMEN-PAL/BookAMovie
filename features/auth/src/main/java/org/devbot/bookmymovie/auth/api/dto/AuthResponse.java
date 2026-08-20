package org.devbot.bookmymovie.auth.api.dto;

import org.devbot.bookmymovie.user.api.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserResponse user
) {}
