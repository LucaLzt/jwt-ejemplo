package com.ejemplos.jwt.infrastructure.persistence.repository;

import com.ejemplos.jwt.infrastructure.persistence.entity.RevokedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la Lista Negra de Tokens (Blacklist).
 * <p>
 * Se utiliza principalmente para verificar si un Access Token (JTI) ha sido
 * revocado antes de procesar una petición.
 * </p>
 */
@Repository
public interface SpringDataRevokedTokenRepository extends JpaRepository<RevokedTokenEntity, Long> {

    /**
     * Verifica si existe un registro de revocación para un ID de token específico.
     * @param jti JWT ID único.
     * @return true si el token está en la lista negra.
     */
    boolean existsByJti(String jti);

}
