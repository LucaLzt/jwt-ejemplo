package com.ejemplos.jwt.infrastructure.persistence.entity;

import com.ejemplos.jwt.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidad JPA que representa la tabla 'users' en la base de datos.
 * <p>
 * Es la proyección de persistencia del modelo de dominio {@code User}.
 * Aquí definimos las restricciones de columnas (nullable, unique, etc.).
 * </p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
