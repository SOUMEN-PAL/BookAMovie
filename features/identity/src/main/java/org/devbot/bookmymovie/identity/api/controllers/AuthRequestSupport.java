package org.devbot.bookmymovie.identity.api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public final class AuthRequestSupport {

    private AuthRequestSupport() {}

    public static String requireBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing Bearer access token");
        }
        return header.substring(7).trim();
    }

    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
