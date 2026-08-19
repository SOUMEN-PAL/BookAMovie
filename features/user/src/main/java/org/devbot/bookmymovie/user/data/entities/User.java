package org.devbot.bookmymovie.user.data.entities;

import jakarta.persistence.*;
import lombok.*;
import org.devbot.bookmymovie.core.persistance.AuditableEntity;
import org.devbot.bookmymovie.core.security.Role;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class User extends AuditableEntity {
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Session> sessions;
}
