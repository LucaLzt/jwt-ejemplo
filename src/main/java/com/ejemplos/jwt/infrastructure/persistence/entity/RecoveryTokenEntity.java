package com.ejemplos.jwt.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidad JPA para la tabla 'recovery_tokens'.
 * <p>
 * Persiste los tokens de un solo uso generados durante el flujo de
 * recuperación de contraseña.
 * </p>
 */
@Entity
@Table(name = "recovery_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean used;
}
