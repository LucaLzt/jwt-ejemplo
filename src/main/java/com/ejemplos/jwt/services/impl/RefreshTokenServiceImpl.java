package com.ejemplos.jwt.services.impl;

import com.ejemplos.jwt.models.entities.RefreshToken;
import com.ejemplos.jwt.models.entities.User;
import com.ejemplos.jwt.repositories.RefreshTokenRepository;
import com.ejemplos.jwt.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del servicio para la gestión de refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:604800}") // 7 días por defecto
    private long refreshTokenExpiration;

    /**
     * Crea un nuevo refresh token para un usuario.
     *
     * @param user El usuario para el cual crear el token.
     * @return El refresh token creado.
     */
    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Revoca todos los tokens existentes del usuario
        revokeAllTokensByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Busca un refresh token por su valor.
     *
     * @param token El valor del token a buscar.
     * @return Optional con el refresh token si existe y es válido.
     */
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verifica si un refresh token es válido.
     *
     * @param token El refresh token a verificar.
     * @return El refresh token si es válido.
     * @throws RuntimeException si el token ha expirado o está revocado.
     */
    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token has expired");
        }

        return token;
    }

    /**
     * Revoca un refresh token específico.
     *
     * @param token El token a revocar.
     */
    @Override
    @Transactional
    public void revokeToken(RefreshToken token) {
        token.revoke();
        refreshTokenRepository.save(token);
    }

    /**
     * Revoca todos los refresh tokens de un usuario.
     *
     * @param user El usuario cuyos tokens serán revocados.
     */
    @Override
    @Transactional
    public void revokeAllTokensByUser(User user) {
        refreshTokenRepository.revokeAllTokensByUser(user);
    }

    /**
     * Elimina todos los tokens expirados de la base de datos.
     */
    @Override
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
