package com.ejemplos.jwt.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un token de actualización (refresh token) en el sistema.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Verifica si el refresh token ha expirado.
     *
     * @return true si el token ha expirado, false en caso contrario.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    /**
     * Revoca el refresh token.
     */
    public void revoke() {
        this.isRevoked = true;
    }
}
