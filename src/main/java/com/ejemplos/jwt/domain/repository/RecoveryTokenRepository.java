package com.ejemplos.jwt.domain.repository;

import com.ejemplos.jwt.domain.model.RecoveryToken;

import java.util.Optional;

/**
 * Puerto de Salida (Repository) para los tokens de recuperación de contraseña.
 * <p>
 * Gestiona el ciclo de vida de los tokens temporales enviados por correo.
 * </p>
 */
public interface RecoveryTokenRepository {

    RecoveryToken save(RecoveryToken passwordResetToken);

    Optional<RecoveryToken> findByToken(String token);

}
