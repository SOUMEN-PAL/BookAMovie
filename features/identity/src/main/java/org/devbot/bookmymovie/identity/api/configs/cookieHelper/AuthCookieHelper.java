package org.devbot.bookmymovie.identity.api.configs.cookieHelper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthCookieHelper {

    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    public void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }

    public String requireRefreshToken(HttpServletRequest request) {
        return readCookie(request, REFRESH_TOKEN_COOKIE)
                .orElseThrow(() -> new AuthenticationServiceException("RefreshToken not found in cookies"));
    }

    private Optional<String> readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}