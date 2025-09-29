package com.ejemplos.jwt.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Representa un token de recuperación de contraseña (UUID) con expiración corta.
 * Se usa una sola vez para permitir al usuario definir una nueva contraseña.
 *
 * El @Index sobre "expires_at" optimiza la limpieza de tokens vencidos.
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_pwdreset_expires", columnList = "expires_at")
})
@Getter @Setter
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

}
