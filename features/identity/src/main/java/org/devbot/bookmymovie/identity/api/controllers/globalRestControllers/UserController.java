package org.devbot.bookmymovie.identity.api.controllers.globalRestControllers;

import lombok.RequiredArgsConstructor;
import org.devbot.bookmymovie.shared.api.GlobalApi;
import org.devbot.bookmymovie.identity.api.dto.UserResponse;
import org.devbot.bookmymovie.identity.api.mappers.UserMapper;
import org.devbot.bookmymovie.identity.data.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@GlobalApi
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @GetMapping("/user/me")
    @PreAuthorize("hasAuthority('USER_PROFILE_READ')")
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}
