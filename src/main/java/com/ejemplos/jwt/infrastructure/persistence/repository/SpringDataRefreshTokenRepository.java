package com.ejemplos.jwt.infrastructure.persistence.repository;

import com.ejemplos.jwt.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la gestión de Refresh Tokens.
 * <p>
 * Maneja el ciclo de vida de las sesiones persistentes en la base de datos.
 * </p>
 */
@Repository
public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    /** Busca un token por su valor de String único. */
    Optional<RefreshTokenEntity> findByToken(String token);

    /**
     * Revoca masivamente todos los tokens de un usuario.
     * <p>
     * Se usa una consulta JPQL personalizada (@Query) para optimizar el rendimiento,
     * actualizando todos los registros en una sola operación de base de datos
     * en lugar de hacerlo uno por uno.
     * </p>
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(Long userId);

}
