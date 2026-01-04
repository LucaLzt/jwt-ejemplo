package com.ejemplos.jwt.infrastructure.persistence.mapper;

import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

/**
 * Componente MapStruct encargado de la conversión entre el modelo de dominio {@link User}
 * y la entidad de persistencia {@link UserEntity}.
 * <p>
 * Permite que la capa de dominio permanezca agnóstica a la base de datos.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /** Convierte una entidad de base de datos a un modelo de dominio puro. */
    User toDomain(UserEntity userEntity);

    /** Convierte un modelo de dominio a una entidad lista para persistir. */
    UserEntity toEntity(User user);
}
