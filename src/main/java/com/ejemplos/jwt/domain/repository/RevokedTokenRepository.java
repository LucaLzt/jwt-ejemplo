package com.ejemplos.jwt.domain.repository;

import com.ejemplos.jwt.domain.model.RevokedToken;

public interface RevokedTokenRepository {
    void save(RevokedToken revokedToken);
    boolean isRevoked(String jti);
}
