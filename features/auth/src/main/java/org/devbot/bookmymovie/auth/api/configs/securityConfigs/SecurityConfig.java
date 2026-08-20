package org.devbot.bookmymovie.auth.api.configs.securityConfigs;

import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.auth.api.configs.filters.JwtAuthFilter;
import org.devbot.bookmymovie.auth.domain.configs.AuthJwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthJwtProperties.class)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthfilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                // Uses the CorsConfigurationSource bean below (browser cross-origin rules).
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                                // public auth
                                .requestMatchers(
                                        "/api/v1/auth/register",
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/refresh"
                                ).permitAll()
                                // optional ops
                                .requestMatchers(
                                        "/actuator/health",
                                        "/actuator/info"
                                ).permitAll()
                                // payment webhook later:
                                // .requestMatchers(HttpMethod.POST, "/api/v1/payments/webhook").permitAll()
                                // everything else needs a valid JWT (filter sets SecurityContext)
                                .anyRequest().authenticated()
                )
                // Run JWT auth inside Spring Security's chain (before username/password filter).
                .addFilterBefore(jwtAuthfilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * JwtAuthFilter is a {@code @Component} {@link jakarta.servlet.Filter}, so Spring Boot would
     * also register it on the servlet filter chain automatically.
     * <p>
     * We already add the same bean to {@link SecurityFilterChain} via {@code addFilterBefore}.
     * If servlet registration stayed enabled, the filter would run twice per request
     * (servlet chain + Security chain): double JWT parse, double DB checks, odd auth bugs.
     * <p>
     * {@code setEnabled(false)} disables only the servlet registration. The bean still exists
     * and Security still uses it — just once, inside the Security filter chain.
     */
    @Bean
    FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterFilterRegistrationBean(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Exposes Spring Security's {@link AuthenticationManager} so login/AuthService can inject it
     * and verify email+password via {@code authenticate(UsernamePasswordAuthenticationToken)}.
     * Not used by the JWT filter (that path trusts a signed access token instead).
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * CORS = Cross-Origin Resource Sharing. Browsers block frontend JS on one origin
     * (e.g. http://localhost:3000) from calling an API on another (e.g. http://localhost:8080)
     * unless the API responds with the right Access-Control-* headers.
     * <p>
     * {@code .cors(Customizer.withDefaults())} in {@link #securityFilterChain} looks up this bean
     * and applies it. Without it, browser preflight (OPTIONS) / cross-origin calls fail even
     * when Postman/curl work fine.
     * <p>
     * Current settings are permissive (dev-friendly): any origin pattern, any method, any header,
     * for all paths ({@code /**}). Tighten {@code allowedOriginPatterns} for production
     * (e.g. only your real web/app origins) instead of {@code *}.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Which frontend origins may call the API (* = any; prefer explicit URLs in prod).
        config.setAllowedOriginPatterns(List.of("*"));
        // Which HTTP methods are allowed (GET, POST, PUT, ...).
        config.setAllowedMethods(List.of("*"));
        // Which request headers the browser may send (Authorization, Content-Type, ...).
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS config to every path.
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
