package org.devbot.bookmymovie.user.domain.service;

import org.devbot.bookmymovie.user.data.entities.Session;
import org.devbot.bookmymovie.user.data.entities.SessionType;
import org.devbot.bookmymovie.user.data.entities.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessionService {

    Session createSession(
            User user,
            String refreshTokenHash,
            SessionType sessionType,
            Instant expiresAt,
            String ipAddress
    );

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    Optional<Session> findActiveById(Long sessionId);

    Optional<Session> findActiveByIdForUser(Long sessionId, Long userId);

    Session rotateRefreshToken(
            Long sessionId,
            String newRefreshTokenHash,
            Instant newExpiresAt
    );

    void touchLastUsed(Long sessionId);

    void revokeSession(Long sessionId);

    void revokeAllUserSessions(Long userId);

    List<Session> getAllUserSessions(Long userId);

    void revokeUserSession(Long userId, Long sessionId);
}
