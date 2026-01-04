package com.ejemplos.jwt.infrastructure.persistence.mapper;

import com.ejemplos.jwt.domain.model.RevokedToken;
import com.ejemplos.jwt.infrastructure.persistence.entity.RevokedTokenEntity;
import org.mapstruct.Mapper;

/**
 * Mapper para la entidad de Lista Negra (Blacklist).
 * <p>
 * Realiza la conversión entre {@link RevokedToken} y {@link RevokedTokenEntity}.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface RevokedTokenMapper {

    RevokedTokenEntity toEntity(RevokedToken revokedToken);

    RevokedToken toDomain(RevokedTokenEntity revokedTokenEntity);
}
