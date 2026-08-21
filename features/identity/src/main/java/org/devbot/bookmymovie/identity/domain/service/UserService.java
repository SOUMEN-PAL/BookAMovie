package org.devbot.bookmymovie.identity.domain.service;

import org.devbot.bookmymovie.identity.data.entities.User;
import org.devbot.bookmymovie.identity.data.entities.UserStatus;

import java.util.Optional;

public interface UserService {

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    User getById(Long userId);

    User getByEmail(String email);

    boolean existsByEmail(String email);

    User createUser(String email, String passwordHash, String name);

    User updateProfile(Long userId, String name);

    User updatePassword(Long userId, String passwordHash);

    User updateStatus(Long userId, UserStatus status);
}
