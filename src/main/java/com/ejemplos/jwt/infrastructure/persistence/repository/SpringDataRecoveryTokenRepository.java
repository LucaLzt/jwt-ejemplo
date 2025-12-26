package com.ejemplos.jwt.infrastructure.persistence.repository;

import com.ejemplos.jwt.infrastructure.persistence.entity.RecoveryTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataRecoveryTokenRepository extends JpaRepository<RecoveryTokenEntity, Long> {

    Optional<RecoveryTokenEntity> findByToken(String token);

}
