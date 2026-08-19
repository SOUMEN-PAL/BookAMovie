package org.devbot.bookmymovie.user.data.service;

import org.devbot.bookmymovie.user.api.dto.UserResponse;
import org.devbot.bookmymovie.user.data.entities.User;

public interface UserService {
    UserResponse getUserByEmail(String email);

    UserResponse saveUser(User user);
}
