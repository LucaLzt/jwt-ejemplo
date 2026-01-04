package com.ejemplos.jwt.infrastructure.persistence.mapper;

import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.infrastructure.persistence.entity.RecoveryTokenEntity;
import org.mapstruct.Mapper;

/**
 * Mapper para la transformación de objetos de Tokens de Recuperación.
 * <p>
 * Traduce entre {@link RecoveryToken} (Dominio) y {@link RecoveryTokenEntity} (Persistencia).
 * </p>
 */
@Mapper(componentModel = "spring")
public interface RecoveryTokenMapper {

    RecoveryTokenEntity toEntity(RecoveryToken recoveryToken);

    RecoveryToken toDomain(RecoveryTokenEntity recoveryToken);
}
