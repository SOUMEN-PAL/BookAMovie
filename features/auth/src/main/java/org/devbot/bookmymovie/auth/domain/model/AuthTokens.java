package org.devbot.bookmymovie.auth.domain.model;

import org.devbot.bookmymovie.user.data.entities.User;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        User user
) {}
