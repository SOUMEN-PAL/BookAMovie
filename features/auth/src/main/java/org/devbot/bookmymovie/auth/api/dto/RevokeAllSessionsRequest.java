package org.devbot.bookmymovie.auth.api.dto;

import jakarta.validation.constraints.NotNull;

public record RevokeAllSessionsRequest(
        @NotNull Long userId
) {}
