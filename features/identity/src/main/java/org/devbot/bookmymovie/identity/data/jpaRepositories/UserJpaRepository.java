package org.devbot.bookmymovie.identity.data.jpaRepositories;

import org.devbot.bookmymovie.identity.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
