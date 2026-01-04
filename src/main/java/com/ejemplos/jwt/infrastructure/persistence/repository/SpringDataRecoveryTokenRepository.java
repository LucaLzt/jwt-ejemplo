package com.ejemplos.jwt.infrastructure.persistence.repository;

import com.ejemplos.jwt.infrastructure.persistence.entity.RecoveryTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para los Tokens de Recuperación de Contraseña.
 * <p>
 * Permite buscar los tokens temporales enviados por correo para validar
 * solicitudes de cambio de contraseña.
 * </p>
 */
@Repository
public interface SpringDataRecoveryTokenRepository extends JpaRepository<RecoveryTokenEntity, Long> {

    /** Busca el token de recuperación por su código UUID. */
    Optional<RecoveryTokenEntity> findByToken(String token);

}
