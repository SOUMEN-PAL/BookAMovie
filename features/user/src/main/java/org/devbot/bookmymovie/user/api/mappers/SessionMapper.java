package org.devbot.bookmymovie.user.api.mappers;

import org.devbot.bookmymovie.user.api.dto.SessionResponse;
import org.devbot.bookmymovie.user.data.entities.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SessionMapper {

    @Mapping(target = "userId", source = "user.id")
    SessionResponse toResponse(Session session);
}
