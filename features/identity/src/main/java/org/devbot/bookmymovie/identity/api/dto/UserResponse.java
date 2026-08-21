package org.devbot.bookmymovie.identity.api.dto;

import java.io.Serializable;
import org.devbot.bookmymovie.core.security.Role;
import org.devbot.bookmymovie.identity.data.entities.UserStatus;

public record UserResponse(
        Long id,
        String email,
        String name,
        Role role,
        UserStatus status
) implements Serializable {}
