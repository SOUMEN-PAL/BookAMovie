package org.devbot.bookmymovie.identity.domain.service;

import org.devbot.bookmymovie.identity.data.entities.User;

public interface JwtService {

    String generateAccessToken(User user, Long sessionId);

    String generateRefreshToken(User user);

    Long getUserIdFromAccessToken(String token);

    Long getUserIdFromRefreshToken(String token);

    Long getSessionIdFromAccessToken(String token);
}
