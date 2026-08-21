package org.devbot.bookmymovie.identity.api.mappers;

import org.devbot.bookmymovie.identity.api.dto.UserResponse;
import org.devbot.bookmymovie.identity.data.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);
}
