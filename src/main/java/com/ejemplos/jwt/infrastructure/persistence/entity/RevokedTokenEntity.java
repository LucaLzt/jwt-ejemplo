package com.ejemplos.jwt.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidad JPA para la tabla 'revoked_tokens' (Blacklist).
 * <p>
 * Almacena los identificadores (JTI) de los tokens que han sido invalidados
 * explícitamente antes de su expiración natural.
 * </p>
 */
@Entity
@Table(name = "revoked_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevokedTokenEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jti;

    private String subject;

    private String reason;

    @Column(nullable = false, name = "expires_at")
    private Instant expiresAt;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;
}
