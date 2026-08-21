package org.devbot.bookmymovie.identity.api.dto;

import jakarta.validation.constraints.NotNull;

public record RevokeAllSessionsRequest(
        @NotNull Long userId
) {}
