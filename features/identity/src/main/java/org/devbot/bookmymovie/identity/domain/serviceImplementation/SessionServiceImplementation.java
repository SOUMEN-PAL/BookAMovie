package org.devbot.bookmymovie.identity.domain.serviceImplementation;

import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.shared.exception.SessionExpiredException;
import org.devbot.bookmymovie.shared.exception.SessionNotFoundException;
import org.devbot.bookmymovie.shared.exception.SessionRevokedException;
import org.devbot.bookmymovie.identity.data.entities.Session;
import org.devbot.bookmymovie.identity.data.entities.SessionType;
import org.devbot.bookmymovie.identity.data.entities.User;
import org.devbot.bookmymovie.identity.data.jpaRepositories.SessionJpaRepository;
import org.devbot.bookmymovie.identity.domain.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionServiceImplementation implements SessionService {

    private final SessionJpaRepository repo;

    @Override
    public Session createSession(
            User user,
            String refreshTokenHash,
            SessionType sessionType,
            Instant expiresAt,
            String ipAddress
    ) {
        Session session = Session.builder()
                .user(user)
                .refreshTokenHash(refreshTokenHash)
                .sessionType(sessionType)
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .lastUsedAt(Instant.now())
                .build();
        return repo.save(session);
    }

    @Override
    public Optional<Session> findByRefreshTokenHash(String refreshTokenHash) {
        return repo.getSessionsByRefreshTokenHash(refreshTokenHash);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> findActiveById(Long sessionId) {
        return repo.findById(sessionId).filter(this::isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> findActiveByIdForUser(Long sessionId, Long userId) {
        return repo.findByIdAndUser_Id(sessionId, userId).filter(this::isActive);
    }

    private boolean isActive(Session session) {
        return session.getRevokedAt() == null
                && session.getExpiresAt().isAfter(Instant.now());
    }

    @Override
    @Transactional
    public Session rotateRefreshToken(
            Long sessionId,
            String newRefreshTokenHash,
            Instant newExpiresAt
    ) {
        Session session = repo.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        if (session.getRevokedAt() != null) {
            throw new SessionRevokedException(sessionId);
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new SessionExpiredException(sessionId);
        }
        session.setRefreshTokenHash(newRefreshTokenHash);
        session.setExpiresAt(newExpiresAt);
        session.setLastUsedAt(Instant.now());
        return session;
    }

    @Override
    @Transactional
    public void touchLastUsed(Long sessionId) {
        Session session = repo.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        session.setLastUsedAt(Instant.now());
    }

    @Override
    @Transactional
    public void revokeSession(Long sessionId) {
        Session session = repo.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        if (session.getRevokedAt() == null) {
            session.setRevokedAt(Instant.now());
        }
    }

    @Override
    @Transactional
    public void revokeAllUserSessions(Long userId) {
        Instant now = Instant.now();
        List<Session> sessions = repo.getSessionsByUser_Id(userId);
        for (Session session : sessions) {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(now);
            }
        }
    }

    @Override
    public List<Session> getAllUserSessions(Long userId) {
        return repo.getSessionsByUser_Id(userId);
    }

    @Override
    @Transactional
    public void revokeUserSession(Long userId, Long sessionId) {
        Session session = repo.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        if (session.getRevokedAt() == null) {
            session.setRevokedAt(Instant.now());
        }
    }
}
