package org.devbot.bookmymovie.user.data.entities;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import org.devbot.bookmymovie.core.persistance.AuditableEntity;
import org.devbot.bookmymovie.core.security.Role;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
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
