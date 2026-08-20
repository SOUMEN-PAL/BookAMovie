package org.devbot.bookmymovie.auth.domain.serviceImplementation;

import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.auth.domain.configs.AuthJwtProperties;
import org.devbot.bookmymovie.auth.domain.model.AuthTokens;
import org.devbot.bookmymovie.auth.domain.service.AuthService;
import org.devbot.bookmymovie.auth.domain.service.JwtService;
import org.devbot.bookmymovie.shared.exception.SessionExpiredException;
import org.devbot.bookmymovie.shared.exception.SessionNotFoundException;
import org.devbot.bookmymovie.shared.exception.SessionRevokedException;
import org.devbot.bookmymovie.user.data.entities.Session;
import org.devbot.bookmymovie.user.data.entities.SessionType;
import org.devbot.bookmymovie.user.data.entities.User;
import org.devbot.bookmymovie.user.data.entities.UserStatus;
import org.devbot.bookmymovie.user.domain.service.SessionService;
import org.devbot.bookmymovie.user.domain.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements AuthService {

    private final UserService userService;
    private final SessionService sessionService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthJwtProperties jwtProperties;

    @Override
    @Transactional
    public AuthTokens register(
            String name,
            String email,
            String rawPassword,
            SessionType sessionType,
            String ipAddress
    ) {
        User user = userService.createUser(email, passwordEncoder.encode(rawPassword), name);
        return issueTokens(user, sessionTypeOrDefault(sessionType), ipAddress);
    }

    @Override
    @Transactional
    public AuthTokens login(
            String email,
            String rawPassword,
            SessionType sessionType,
            String ipAddress
    ) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("User account is not active");
        }
        return issueTokens(user, sessionTypeOrDefault(sessionType), ipAddress);
    }

    @Override
    @Transactional
    public AuthTokens refresh(String refreshToken) {
        String refreshTokenHash = hashToken(refreshToken);
        Session session = sessionService.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(SessionNotFoundException::new);

        if (session.getRevokedAt() != null) {
            throw new SessionRevokedException(session.getId());
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new SessionExpiredException(session.getId());
        }

        User user = session.getUser();
        String newRefreshToken = jwtService.generateRefreshToken(user);
        Instant newExpiresAt = Instant.now().plus(jwtProperties.refresh().ttl());
        sessionService.rotateRefreshToken(session.getId(), hashToken(newRefreshToken), newExpiresAt);

        String accessToken = jwtService.generateAccessToken(user, session.getId());
        return new AuthTokens(
                accessToken,
                newRefreshToken,
                jwtProperties.access().ttl().toSeconds(),
                user
        );
    }

    @Override
    @Transactional
    public void logout(Long sessionId) {
        sessionService.revokeSession(sessionId);
    }

    @Override
    @Transactional
    public void revokeSession(Long userId, Long sessionId) {
        sessionService.revokeUserSession(userId, sessionId);
    }

    @Override
    @Transactional
    public void revokeAllSessions(Long userId) {
        sessionService.revokeAllUserSessions(userId);
    }

    private AuthTokens issueTokens(User user, SessionType sessionType, String ipAddress) {
        String refreshToken = jwtService.generateRefreshToken(user);
        Instant expiresAt = Instant.now().plus(jwtProperties.refresh().ttl());
        Session session = sessionService.createSession(
                user,
                hashToken(refreshToken),
                sessionType,
                expiresAt,
                ipAddress
        );
        String accessToken = jwtService.generateAccessToken(user, session.getId());
        return new AuthTokens(
                accessToken,
                refreshToken,
                jwtProperties.access().ttl().toSeconds(),
                user
        );
    }

    private static SessionType sessionTypeOrDefault(SessionType sessionType) {
        return sessionType != null ? sessionType : SessionType.WEB;
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
