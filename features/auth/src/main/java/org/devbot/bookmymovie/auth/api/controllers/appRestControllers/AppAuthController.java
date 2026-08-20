package org.devbot.bookmymovie.auth.api.controllers.appRestControllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.auth.api.controllers.AuthRequestSupport;
import org.devbot.bookmymovie.auth.api.dto.AuthResponse;
import org.devbot.bookmymovie.auth.api.dto.LoginRequest;
import org.devbot.bookmymovie.auth.api.dto.RefreshRequest;
import org.devbot.bookmymovie.auth.api.dto.RegisterRequest;
import org.devbot.bookmymovie.auth.api.dto.RevokeAllSessionsRequest;
import org.devbot.bookmymovie.auth.api.dto.RevokeSessionRequest;
import org.devbot.bookmymovie.auth.api.mappers.AuthMapper;
import org.devbot.bookmymovie.auth.domain.model.AuthTokens;
import org.devbot.bookmymovie.auth.domain.service.AuthService;
import org.devbot.bookmymovie.auth.domain.service.JwtService;
import org.devbot.bookmymovie.shared.api.AppApi;
import org.devbot.bookmymovie.user.data.entities.SessionType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@AppApi
@RequiredArgsConstructor
public class AppAuthController {

    private final AuthService authService;
    private final AuthMapper authMapper;
    private final JwtService jwtService;

    @PostMapping({"/auth/register", "/auth/signup"})
    public ResponseEntity<AuthResponse> signUp(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthTokens tokens = authService.register(
                request.name(),
                request.email(),
                request.password(),
                SessionType.OTHERS,
                AuthRequestSupport.clientIp(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(authMapper.toResponse(tokens));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        SessionType sessionType = request.sessionType() != null
                ? request.sessionType()
                : SessionType.OTHERS;
        AuthTokens tokens = authService.login(
                request.email(),
                request.password(),
                sessionType,
                AuthRequestSupport.clientIp(httpRequest)
        );
        return ResponseEntity.ok(authMapper.toResponse(tokens));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthTokens tokens = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(authMapper.toResponse(tokens));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        Long sessionId = jwtService.getSessionIdFromAccessToken(
                AuthRequestSupport.requireBearerToken(httpRequest)
        );
        authService.logout(sessionId);
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
}
