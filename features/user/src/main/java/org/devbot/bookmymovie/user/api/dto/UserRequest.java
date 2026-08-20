package org.devbot.bookmymovie.user.api.dto;

public record UserRequest(
        String email,
        String password,
        String name
) {}
