package org.devbot.bookmymovie.identity.api.dto;

import jakarta.validation.constraints.NotNull;

public record RevokeSessionRequest(
        @NotNull Long userId,
        @NotNull Long sessionId
) {}
