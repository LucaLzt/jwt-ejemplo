package com.ejemplos.jwt.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidad JPA que mapea la tabla 'refresh_tokens'.
 * <p>
 * Gestiona la persistencia de las sesiones de larga duración y su relación
 * con el usuario propietario (Foreign Key).
 * </p>
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // length = 512 es importante porque los tokens JWT pueden ser largos
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "replaced_by", length = 512)
    private String replacedBy;

    /**
     * Relación Muchos-a-Uno con la tabla de usuarios.
     * Usamos FetchType.LAZY para rendimiento (no traer al usuario si no hace falta).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
