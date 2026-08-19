package org.devbot.bookmymovie.user.data.jpaRepositories;

import org.devbot.bookmymovie.user.data.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionJpaRepository extends JpaRepository<Session , Long> {
}
