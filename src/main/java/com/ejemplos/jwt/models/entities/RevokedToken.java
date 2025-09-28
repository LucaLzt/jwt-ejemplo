package com.ejemplos.jwt.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/*
 * Entidad que representa un token JWT revocado en la base de datos (Blacklist).
 *
 * El @Index sobre "expires_at" optimiza las búsquedas y eliminaciones
 * de tokens vencidos, haciendo más eficiente el job de limpieza.
 */
@Entity
@Table(name = "revoked_tokens", indexes = {
        @Index(name = "idx_revoked_expires", columnList = "expires_at")
})
@Getter @Setter
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    @Column(name = "subject")
    private String subject;

    @Column(name = "reason", length = 50)
    private String reason;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

}
