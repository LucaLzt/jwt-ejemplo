package com.ejemplos.jwt.infrastructure.persistence.repository;

import com.ejemplos.jwt.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad de Usuarios.
 * <p>
 * Extiende de {@link JpaRepository} para obtener métodos CRUD estándar
 * y define consultas derivadas (Query Methods) para búsquedas por email.
 * </p>
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {

    /** Busca un usuario por su correo electrónico exacto. */
    Optional<UserEntity> findByEmail(String email);

    /** Verifica eficientemente si un correo ya está registrado en la base de datos. */
    boolean existsByEmail(String email);

}
