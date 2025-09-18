package com.ejemplos.jwt.services;

import com.ejemplos.jwt.models.entities.RefreshToken;
import com.ejemplos.jwt.models.entities.User;

import java.util.Optional;

/**
 * Interfaz que define los servicios para la gestión de refresh tokens.
 */
public interface RefreshTokenService {

    /**
     * Crea un nuevo refresh token para un usuario.
     *
     * @param user El usuario para el cual crear el token.
     * @return El refresh token creado.
     */
    RefreshToken createRefreshToken(User user);

    /**
     * Busca un refresh token por su valor.
     *
     * @param token El valor del token a buscar.
     * @return Optional con el refresh token si existe y es válido.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Verifica si un refresh token es válido.
     *
     * @param token El refresh token a verificar.
     * @return El refresh token si es válido.
     * @throws RuntimeException si el token ha expirado o está revocado.
     */
    RefreshToken verifyExpiration(RefreshToken token);

    /**
     * Revoca un refresh token específico.
     *
     * @param token El token a revocar.
     */
    void revokeToken(RefreshToken token);

    /**
     * Revoca todos los refresh tokens de un usuario.
     *
     * @param user El usuario cuyos tokens serán revocados.
     */
    void revokeAllTokensByUser(User user);

    /**
     * Elimina todos los tokens expirados de la base de datos.
     */
    void deleteExpiredTokens();
}
