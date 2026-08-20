package org.devbot.bookmymovie.auth.api.configs.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.auth.domain.dataModels.BookMyMovieUserDetails;
import org.devbot.bookmymovie.auth.domain.service.JwtService;
import org.devbot.bookmymovie.shared.exception.SessionNotFoundException;
import org.devbot.bookmymovie.user.data.entities.User;
import org.devbot.bookmymovie.user.domain.service.SessionService;
import org.devbot.bookmymovie.user.domain.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final SessionService sessionService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String requestTokenHeader = request.getHeader("Authorization");

            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String accessToken = requestTokenHeader.substring(7).trim();
            Long userId = jwtService.getUserIdFromAccessToken(accessToken);
            Long sessionId = jwtService.getSessionIdFromAccessToken(accessToken);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getById(userId);
                sessionService.findActiveByIdForUser(sessionId, userId)
                        .orElseThrow(() -> new SessionNotFoundException(sessionId));

                BookMyMovieUserDetails principal = new BookMyMovieUserDetails(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}
