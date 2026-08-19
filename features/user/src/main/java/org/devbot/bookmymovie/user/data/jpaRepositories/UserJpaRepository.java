package org.devbot.bookmymovie.user.data.jpaRepositories;

import org.devbot.bookmymovie.core.security.Permission;
import org.devbot.bookmymovie.core.security.Role;
import org.devbot.bookmymovie.user.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    User getUserByEmail(String email);

}
