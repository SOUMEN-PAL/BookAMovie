package org.devbot.bookmymovie.user.api.mappers;

import org.devbot.bookmymovie.user.api.dto.UserResponse;
import org.devbot.bookmymovie.user.data.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);
}
