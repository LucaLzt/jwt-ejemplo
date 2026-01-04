package com.ejemplos.jwt.infrastructure.persistence.adapter;

import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.UserRepository;
import com.ejemplos.jwt.infrastructure.persistence.entity.UserEntity;
import com.ejemplos.jwt.infrastructure.persistence.mapper.UserMapper;
import com.ejemplos.jwt.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de Persistencia para la gestión de Usuarios.
 * <p>
 * Implementa el puerto {@link UserRepository} utilizando Spring Data JPA.
 * Su responsabilidad es traducir las llamadas del dominio a operaciones de base de datos
 * y mapear los resultados (Entidades) de vuelta a objetos de dominio.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity userEntity = springDataUserRepository.save(userMapper.toEntity(user));
        return userMapper.toDomain(userEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }
}

