package com.ejemplos.jwt.infrastructure.persistence.mapper;

import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.infrastructure.persistence.entity.RecoveryTokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecoveryTokenMapper {

    RecoveryTokenEntity toEntity(RecoveryToken recoveryToken);
    RecoveryToken toDomain(RecoveryTokenEntity recoveryToken);

}
