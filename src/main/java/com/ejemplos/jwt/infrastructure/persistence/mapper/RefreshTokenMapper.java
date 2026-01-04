package com.ejemplos.jwt.infrastructure.persistence.mapper;

import com.ejemplos.jwt.domain.model.RefreshToken;
import com.ejemplos.jwt.infrastructure.persistence.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper encargado de convertir los Refresh Tokens.
 * <p>
 * Gestiona la conversión incluyendo el mapeo de claves foráneas (ID de usuario)
 * entre el objeto de dominio {@link RefreshToken} y la entidad JPA {@link RefreshTokenEntity}.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    @Mapping(target = "userId", source = "user.id")
    RefreshToken toDomain(RefreshTokenEntity refreshTokenEntity);

    @Mapping(target = "user.id", source = "userId")
    RefreshTokenEntity toEntity(RefreshToken refreshToken);
}
