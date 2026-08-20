package org.devbot.bookmymovie.auth.api.dto;

import jakarta.validation.constraints.NotNull;

public record RevokeSessionRequest(
        @NotNull Long userId,
        @NotNull Long sessionId
) {}
