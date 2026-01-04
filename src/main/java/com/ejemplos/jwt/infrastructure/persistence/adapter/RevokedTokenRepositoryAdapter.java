package com.ejemplos.jwt.infrastructure.persistence.adapter;

import com.ejemplos.jwt.domain.model.RevokedToken;
import com.ejemplos.jwt.domain.repository.RevokedTokenRepository;
import com.ejemplos.jwt.infrastructure.persistence.entity.RevokedTokenEntity;
import com.ejemplos.jwt.infrastructure.persistence.mapper.RevokedTokenMapper;
import com.ejemplos.jwt.infrastructure.persistence.repository.SpringDataRevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador de Persistencia para la Lista Negra de Tokens.
 * <p>
 * Implementa {@link RevokedTokenRepository} delegando en Spring Data JPA.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class RevokedTokenRepositoryAdapter implements RevokedTokenRepository {

    private final SpringDataRevokedTokenRepository springDataRevokedTokenRepository;
    private final RevokedTokenMapper revokedTokenMapper;

    @Override
    public void save(RevokedToken revokedToken) {
        RevokedTokenEntity toEntity = revokedTokenMapper.toEntity(revokedToken);
        springDataRevokedTokenRepository.save(toEntity);
    }

    @Override
    public boolean isRevoked(String jti) {
        return springDataRevokedTokenRepository.existsByJti(jti);
    }
}
