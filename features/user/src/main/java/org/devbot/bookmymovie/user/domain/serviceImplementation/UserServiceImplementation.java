package org.devbot.bookmymovie.user.domain.serviceImplementation;

import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.user.api.dto.UserResponse;
import org.devbot.bookmymovie.user.data.entities.User;
import org.devbot.bookmymovie.user.data.jpaRepositories.UserJpaRepository;
import org.devbot.bookmymovie.user.data.service.UserService;
import org.devbot.bookmymovie.user.domain.mappers.UserMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserJpaRepository repo;
    private final UserMapper mapper;

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = repo.getUserByEmail(email);
        return mapper.toResponse(user);
    }

    @Override
    public UserResponse saveUser(User user) {
        User savedUser = repo.save(user);
        return mapper.toResponse(savedUser);
    }
}
