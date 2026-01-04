package com.ejemplos.jwt.application.ports.in;

/**
 * Resultado de una operación de rotación de tokens.
 * Devuelve el nuevo par de llaves frescas.
 */
public record RefreshTokenResult(
        String accessToken,
        String refreshToken
) {
}
