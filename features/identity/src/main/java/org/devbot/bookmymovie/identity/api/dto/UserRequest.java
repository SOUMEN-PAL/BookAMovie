package org.devbot.bookmymovie.identity.api.dto;

public record UserRequest(
        String email,
        String password,
        String name
) {}
