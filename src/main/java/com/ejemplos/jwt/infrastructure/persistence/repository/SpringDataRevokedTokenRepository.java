package com.ejemplos.jwt.infrastructure.persistence.repository;

import com.ejemplos.jwt.infrastructure.persistence.entity.RevokedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRevokedTokenRepository extends JpaRepository<RevokedTokenEntity, Long> {
    boolean existsByJti(String jti);
}
