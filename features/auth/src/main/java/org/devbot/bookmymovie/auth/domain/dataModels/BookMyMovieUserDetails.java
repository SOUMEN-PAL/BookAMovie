package org.devbot.bookmymovie.auth.domain.dataModels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.devbot.bookmymovie.auth.domain.configs.RoleAuthorities;
import org.devbot.bookmymovie.user.data.entities.User;
import org.devbot.bookmymovie.user.data.entities.UserStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookMyMovieUserDetails implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return RoleAuthorities.of(user.getRole());
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
