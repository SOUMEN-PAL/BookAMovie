package org.devbot.bookmymovie.identity.domain.model;

import org.devbot.bookmymovie.identity.data.entities.User;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        User user
) {}
