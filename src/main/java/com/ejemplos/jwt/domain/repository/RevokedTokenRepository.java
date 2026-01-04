package com.ejemplos.jwt.domain.repository;

import com.ejemplos.jwt.domain.model.RevokedToken;

/**
 * Puerto de Salida (Repository) para la Lista Negra de tokens (Blacklist).
 * <p>
 * Se encarga de guardar y verificar los identificadores (JTI) de los tokens
 * que han sido invalidados explícitamente antes de expirar.
 * </p>
 */
public interface RevokedTokenRepository {

    void save(RevokedToken revokedToken);

    /**
     * Verifica si un token (por su ID) está en la lista negra.
     * @param jti JWT ID único.
     * @return true si el token fue revocado.
     */
    boolean isRevoked(String jti);

}
