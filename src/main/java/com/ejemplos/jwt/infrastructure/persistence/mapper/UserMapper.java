package com.ejemplos.jwt.infrastructure.persistence.mapper;

import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDomain(UserEntity userEntity);
    UserEntity toEntity(User user);
}
