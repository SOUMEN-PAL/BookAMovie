package org.devbot.bookmymovie.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.devbot.bookmymovie.shared.validation.ValidPassword;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @ValidPassword String password
) {}
