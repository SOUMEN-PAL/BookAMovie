package org.devbot.bookmymovie.identity.domain.service;

public enum JwtClaims {
    EMAIL("email"),
    ROLES("role"),
    SESSION_ID("sid");

    private final String key;

    JwtClaims(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
