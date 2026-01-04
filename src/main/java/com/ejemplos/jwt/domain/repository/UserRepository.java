package com.ejemplos.jwt.domain.repository;

import com.ejemplos.jwt.domain.model.User;

import java.util.Optional;

/**
 * Puerto de Salida (Repository) para la persistencia de Usuarios.
 * <p>
 * Define las operaciones necesarias para guardar y buscar usuarios
 * independientemente de la base de datos subyacente (MySQL, H2, etc.).
 * </p>
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}
