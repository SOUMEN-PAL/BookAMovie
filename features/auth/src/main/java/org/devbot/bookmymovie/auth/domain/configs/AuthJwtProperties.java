package org.devbot.bookmymovie.auth.domain.configs;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bookmymovie.auth.jwt")
public record AuthJwtProperties(Access access, Refresh refresh) {

    public record Access(String secret, Duration ttl) {}

    public record Refresh(String secret, Duration ttl) {}
}
