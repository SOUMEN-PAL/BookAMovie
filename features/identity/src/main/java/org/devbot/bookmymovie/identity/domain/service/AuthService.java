package org.devbot.bookmymovie.identity.domain.service;

import org.devbot.bookmymovie.identity.domain.model.AuthTokens;
import org.devbot.bookmymovie.identity.data.entities.SessionType;

public interface AuthService {

    AuthTokens register(
            String name,
            String email,
            String rawPassword,
            SessionType sessionType,
            String ipAddress
    );

    AuthTokens login(
            String email,
            String rawPassword,
            SessionType sessionType,
            String ipAddress
    );

    AuthTokens refresh(String refreshToken);

    void logout(Long sessionId);

    void revokeSession(Long userId, Long sessionId);

    void revokeAllSessions(Long userId);
}
