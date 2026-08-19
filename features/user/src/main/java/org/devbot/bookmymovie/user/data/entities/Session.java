package org.devbot.bookmymovie.user.data.entities;

import jakarta.persistence.*;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.devbot.bookmymovie.core.persistance.AuditableEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sessions")
public class Session extends AuditableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 64)
    private String refreshTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type")
    private SessionType sessionType;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @Column(length = 45)
    private String ipAddress;

    private Instant lastUsedAt = Instant.now();
}
