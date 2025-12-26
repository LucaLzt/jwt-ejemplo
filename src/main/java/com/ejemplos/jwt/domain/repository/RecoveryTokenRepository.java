package com.ejemplos.jwt.domain.repository;

import com.ejemplos.jwt.domain.model.RecoveryToken;

import java.util.Optional;

public interface RecoveryTokenRepository {

    RecoveryToken save(RecoveryToken passwordResetToken);

    Optional<RecoveryToken> findByToken(String token);

}
