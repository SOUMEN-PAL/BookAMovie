package org.devbot.bookmymovie.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.devbot.bookmymovie.identity.data.entities.SessionType;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        SessionType sessionType
) {
    public LoginRequest {
        if (sessionType == null) {
            sessionType = SessionType.WEB;
        }
    }
}
