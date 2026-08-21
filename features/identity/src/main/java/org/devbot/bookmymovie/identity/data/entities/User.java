package org.devbot.bookmymovie.identity.data.entities;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.List;
import lombok.*;
import org.devbot.bookmymovie.identity.domain.configs.RoleAuthorities;
import org.devbot.bookmymovie.core.persistance.AuditableEntity;
import org.devbot.bookmymovie.core.security.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableEntity implements UserDetails {
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return RoleAuthorities.of(role);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
