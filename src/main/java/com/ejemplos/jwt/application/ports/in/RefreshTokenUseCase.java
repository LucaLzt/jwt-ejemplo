package com.ejemplos.jwt.application.ports.in;

/**
 * Puerto de Entrada (Input Port) para la renovación de tokens.
 * Maneja la lógica de rotación de seguridad.
 */
public interface RefreshTokenUseCase {
    RefreshTokenResult refresh(String refreshTokenValue);
}
