package org.devbot.bookmymovie.identity.api.controllers.webRestControllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.identity.api.configs.cookieHelper.AuthCookieHelper;
import org.devbot.bookmymovie.identity.api.controllers.AuthRequestSupport;
import org.devbot.bookmymovie.identity.api.dto.AuthResponse;
import org.devbot.bookmymovie.identity.api.dto.LoginRequest;
import org.devbot.bookmymovie.identity.api.dto.RefreshRequest;
import org.devbot.bookmymovie.identity.api.dto.RegisterRequest;
import org.devbot.bookmymovie.identity.api.dto.RevokeAllSessionsRequest;
import org.devbot.bookmymovie.identity.api.dto.RevokeSessionRequest;
import org.devbot.bookmymovie.identity.api.mappers.AuthMapper;
import org.devbot.bookmymovie.identity.domain.model.AuthTokens;
import org.devbot.bookmymovie.identity.domain.service.AuthService;
import org.devbot.bookmymovie.identity.domain.service.JwtService;
import org.devbot.bookmymovie.shared.api.WebApi;
import org.devbot.bookmymovie.identity.data.entities.SessionType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@WebApi
@RequiredArgsConstructor
public class WebAuthController {

    private final AuthService authService;
    private final AuthMapper authMapper;
    private final AuthCookieHelper authCookieHelper;
    private final JwtService jwtService;

    @PostMapping({"/auth/register", "/auth/signup"})
    public ResponseEntity<AuthResponse> signUp(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthTokens tokens = authService.register(
                request.name(),
                request.email(),
                request.password(),
                SessionType.WEB,
                AuthRequestSupport.clientIp(httpRequest)
        );
        authCookieHelper.addRefreshCookie(httpResponse, tokens.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(authMapper.toResponse(tokens));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthTokens tokens = authService.login(
                request.email(),
                request.password(),
                SessionType.WEB,
                AuthRequestSupport.clientIp(httpRequest)
        );
        authCookieHelper.addRefreshCookie(httpResponse, tokens.refreshToken());
        return ResponseEntity.ok(authMapper.toResponse(tokens));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody(required = false) RefreshRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refreshToken = resolveRefreshToken(request, httpRequest);
        AuthTokens tokens = authService.refresh(refreshToken);
        authCookieHelper.addRefreshCookie(httpResponse, tokens.refreshToken());
        return ResponseEntity.ok(authMapper.toResponse(tokens));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Long sessionId = jwtService.getSessionIdFromAccessToken(
                AuthRequestSupport.requireBearerToken(httpRequest)
        );
        authService.logout(sessionId);
        authCookieHelper.clearRefreshCookie(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ADMIN_SESSION_REVOKE')")
    @PostMapping("/auth/sessions/revoke")
    public ResponseEntity<Void> revokeSession(@Valid @RequestBody RevokeSessionRequest request) {
        authService.revokeSession(request.userId(), request.sessionId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ADMIN_SESSION_REVOKE')")
    @PostMapping("/auth/sessions/revoke-all")
    public ResponseEntity<Void> revokeAllSessions(@Valid @RequestBody RevokeAllSessionsRequest request) {
        authService.revokeAllSessions(request.userId());
        return ResponseEntity.noContent().build();
    }

    private String resolveRefreshToken(RefreshRequest request, HttpServletRequest httpRequest) {
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            return request.refreshToken();
        }
        return authCookieHelper.requireRefreshToken(httpRequest);
    }
}
