package org.devbot.bookmymovie.identity.api.dto;

import java.time.Instant;
import org.devbot.bookmymovie.identity.data.entities.SessionType;
public record SessionResponse(
        Long userId,
        SessionType sessionType,
        Instant expiresAt,
        Instant revokedAt,
        String ipAddress
) {}