package org.devbot.bookmymovie.identity.domain.serviceImplementation;

import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.core.security.Role;
import org.devbot.bookmymovie.shared.exception.UserAlreadyExistsException;
import org.devbot.bookmymovie.shared.exception.UserNotFoundException;
import org.devbot.bookmymovie.identity.data.entities.User;
import org.devbot.bookmymovie.identity.data.entities.UserStatus;
import org.devbot.bookmymovie.identity.data.jpaRepositories.UserJpaRepository;
import org.devbot.bookmymovie.identity.domain.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserJpaRepository repo;

    @Override
    public Optional<User> findById(Long userId) {
        return repo.findById(userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Override
    public User getById(Long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public User getByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return repo.existsByEmail(email);
    }

    @Override
    @Transactional
    public User createUser(String email, String passwordHash, String name) {
        if (repo.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordHash);
        user.setName(name);
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        return repo.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String name) {
        User user = getById(userId);
        user.setName(name);
        return user;
    }

    @Override
    @Transactional
    public User updatePassword(Long userId, String passwordHash) {
        User user = getById(userId);
        user.setPassword(passwordHash);
        return user;
    }

    @Override
    @Transactional
    public User updateStatus(Long userId, UserStatus status) {
        User user = getById(userId);
        user.setStatus(status);
        return user;
    }
}
