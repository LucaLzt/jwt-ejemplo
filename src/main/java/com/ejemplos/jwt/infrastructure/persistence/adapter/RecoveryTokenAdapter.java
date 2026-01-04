package com.ejemplos.jwt.infrastructure.persistence.adapter;

import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.domain.repository.RecoveryTokenRepository;
import com.ejemplos.jwt.infrastructure.persistence.entity.RecoveryTokenEntity;
import com.ejemplos.jwt.infrastructure.persistence.entity.RefreshTokenEntity;
import com.ejemplos.jwt.infrastructure.persistence.mapper.RecoveryTokenMapper;
import com.ejemplos.jwt.infrastructure.persistence.repository.SpringDataRecoveryTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de Persistencia para los tokens de recuperación de contraseña.
 * <p>
 * Implementa {@link RecoveryTokenRepository} para conectar el dominio con la base de datos MySQL.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class RecoveryTokenAdapter implements RecoveryTokenRepository {

    private final SpringDataRecoveryTokenRepository springDataRecoveryTokenRepository;
    private final RecoveryTokenMapper recoveryTokenMapper;


    @Override
    public RecoveryToken save(RecoveryToken passwordResetToken) {
        RecoveryTokenEntity saved = springDataRecoveryTokenRepository.save(recoveryTokenMapper.toEntity(passwordResetToken));
        return recoveryTokenMapper.toDomain(saved);
    }

    @Override
    public Optional<RecoveryToken> findByToken(String token) {
        return springDataRecoveryTokenRepository.findByToken(token)
                .map(recoveryTokenMapper::toDomain);
    }
}
