package org.devbot.bookmymovie.identity.data.jpaRepositories;

import org.devbot.bookmymovie.identity.data.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionJpaRepository extends JpaRepository<Session , Long> {
    List<Session> getSessionsByUser_Id(Long userId);

    Optional<Session> getSessionsByRefreshTokenHash(String refreshTokenHash);

    Optional<Session> findByIdAndUser_Id(Long id, Long userId);
}
