package org.devbot.bookmymovie.auth.domain.serviceImplementation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.auth.domain.configs.AuthJwtProperties;
import org.devbot.bookmymovie.auth.domain.service.JwtClaims;
import org.devbot.bookmymovie.auth.domain.service.JwtService;
import org.devbot.bookmymovie.user.data.entities.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtServiceImplementation implements JwtService {

    private final AuthJwtProperties jwtProperties;

    private SecretKey getAccessSecret() {
        return Keys.hmacShaKeyFor(
                jwtProperties.access().secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    private SecretKey getRefreshSecret() {
        return Keys.hmacShaKeyFor(
                jwtProperties.refresh().secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public String generateAccessToken(User user, Long sessionId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(JwtClaims.EMAIL.getKey(), user.getEmail())
                .claim(JwtClaims.SESSION_ID.getKey(), sessionId)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.access().ttl().toMillis()))
                .signWith(getAccessSecret())
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.refresh().ttl().toMillis()))
                .signWith(getRefreshSecret())
                .compact();
    }

    private Claims getTokenClaims(String token, SecretKey secret) {
        return Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public Long getUserIdFromAccessToken(String token) {
        String subject = getTokenClaims(token, getAccessSecret()).getSubject();
        return Long.valueOf(subject);
    }

    @Override
    public Long getUserIdFromRefreshToken(String token) {
        String subject = getTokenClaims(token, getRefreshSecret()).getSubject();
        return Long.valueOf(subject);
    }

    @Override
    public Long getSessionIdFromAccessToken(String token) {
        Claims claims = getTokenClaims(token, getAccessSecret());
        Object sid = claims.get(JwtClaims.SESSION_ID.getKey());
        if (sid == null) {
            throw new IllegalArgumentException("Session id is missing in the token");
        }
        return Long.valueOf(sid.toString());
    }
}
