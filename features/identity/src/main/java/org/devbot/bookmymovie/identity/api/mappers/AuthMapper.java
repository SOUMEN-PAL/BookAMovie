package org.devbot.bookmymovie.identity.api.mappers;

import org.devbot.bookmymovie.identity.api.dto.AuthResponse;
import org.devbot.bookmymovie.identity.domain.model.AuthTokens;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = UserMapper.class)
public interface AuthMapper {

    @Mapping(target = "expiresIn", source = "expiresInSeconds")
    @Mapping(target = "user", source = "user")
    AuthResponse toResponse(AuthTokens tokens);
}
