package com.ejemplos.jwt.infrastructure.persistence.mapper;

import com.ejemplos.jwt.domain.model.RefreshToken;
import com.ejemplos.jwt.infrastructure.persistence.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    @Mapping(target = "userId", source = "user.id")
    RefreshToken toDomain(RefreshTokenEntity refreshTokenEntity);

    @Mapping(target = "user.id", source = "userId")
    RefreshTokenEntity toEntity(RefreshToken refreshToken);
}
