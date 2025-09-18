package com.ejemplos.jwt.repositories;

import com.ejemplos.jwt.models.entities.RefreshToken;
import com.ejemplos.jwt.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio para la gestión de tokens de actualización (refresh tokens).
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un refresh token por su valor.
     *
     * @param token El valor del token a buscar.
     * @return Optional con el refresh token si existe.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Busca refresh tokens activos para un usuario específico.
     *
     * @param user El usuario para el cual buscar tokens.
     * @return Optional con el refresh token activo del usuario.
     */
    Optional<RefreshToken> findByUserAndIsRevokedFalse(User user);

    /**
     * Elimina todos los refresh tokens expirados.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Revoca todos los refresh tokens de un usuario.
     *
     * @param user El usuario cuyos tokens serán revocados.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user")
    void revokeAllTokensByUser(@Param("user") User user);
}
