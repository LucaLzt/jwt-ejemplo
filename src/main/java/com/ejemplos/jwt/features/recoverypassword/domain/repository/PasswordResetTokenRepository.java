package com.ejemplos.jwt.features.recoverypassword.domain.repository;

import com.ejemplos.jwt.features.recoverypassword.domain.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repositorio de datos para la entidad PasswordResetToken.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.usedAt IS NOT NULL OR p.expiresAt < :now")
    int deleteAllUsedOrExpired(@Param("now") Instant now);

}
