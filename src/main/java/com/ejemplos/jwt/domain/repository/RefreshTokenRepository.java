package com.ejemplos.jwt.domain.repository;

import com.ejemplos.jwt.domain.model.RefreshToken;

import java.util.Optional;

/**
 * Puerto de Salida (Repository) para la gestión de Refresh Tokens.
 * <p>
 * Permite persistir los tokens de sesión de larga duración.
 * </p>
 */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    /**
     * Revoca todos los tokens activos de un usuario específico.
     * Útil para casos de cambio de contraseña o detección de robo.
     */
    void revokeAllTokens(Long userId);
}
