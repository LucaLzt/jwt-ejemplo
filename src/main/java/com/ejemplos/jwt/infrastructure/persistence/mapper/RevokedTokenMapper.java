package com.ejemplos.jwt.infrastructure.persistence.mapper;

import com.ejemplos.jwt.domain.model.RevokedToken;
import com.ejemplos.jwt.infrastructure.persistence.entity.RevokedTokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RevokedTokenMapper {
    RevokedTokenEntity toEntity(RevokedToken revokedToken);
    RevokedToken toDomain(RevokedTokenEntity revokedTokenEntity);
}
