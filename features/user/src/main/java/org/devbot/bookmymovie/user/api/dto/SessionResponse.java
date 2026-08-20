package org.devbot.bookmymovie.user.api.dto;

import java.time.Instant;
import org.devbot.bookmymovie.user.data.entities.SessionType;
public record SessionResponse(
        Long userId,
        SessionType sessionType,
        Instant expiresAt,
        Instant revokedAt,
        String ipAddress
) {}