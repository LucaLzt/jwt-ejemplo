package com.ejemplos.jwt.infrastructure.persistence.adapter;

import com.ejemplos.jwt.domain.model.RefreshToken;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.infrastructure.persistence.entity.RefreshTokenEntity;
import com.ejemplos.jwt.infrastructure.persistence.mapper.RefreshTokenMapper;
import com.ejemplos.jwt.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de Persistencia para Refresh Tokens.
 * <p>
 * Conecta el puerto de dominio {@link RefreshTokenRepository} con el repositorio JPA real.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;
    private final RefreshTokenMapper refreshTokenMapper;


    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity saved = springDataRefreshTokenRepository.save(refreshTokenMapper.toEntity(refreshToken));
        return refreshTokenMapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springDataRefreshTokenRepository.findByToken(token)
                .map(refreshTokenMapper::toDomain);
    }

    @Override
    public void revokeAllTokens(Long userId) {
        springDataRefreshTokenRepository.revokeAllByUserId(userId);
    }
}
