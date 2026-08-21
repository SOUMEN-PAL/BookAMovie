package org.devbot.bookmymovie.identity.domain.configs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.devbot.bookmymovie.core.security.Permission;
import org.devbot.bookmymovie.core.security.Role;
import org.devbot.bookmymovie.core.security.RolePermissions;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleAuthorities {

    public static Collection<GrantedAuthority> of(Role role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        for (Permission permission : RolePermissions.of(role)) {
            authorities.add(new SimpleGrantedAuthority(permission.name()));
        }
        return List.copyOf(authorities);
    }
}
