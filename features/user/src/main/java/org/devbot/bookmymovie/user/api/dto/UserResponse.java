package org.devbot.bookmymovie.user.api.dto;

import java.io.Serializable;
import org.devbot.bookmymovie.core.security.Role;
import org.devbot.bookmymovie.user.data.entities.UserStatus;

public record UserResponse(
        Long id,
        String email,
        String name,
        Role role,
        UserStatus status
) implements Serializable {}
