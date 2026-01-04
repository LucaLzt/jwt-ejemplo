package com.ejemplos.jwt.application.ports.in;

/**
 * Respuesta exitosa de un inicio de sesión.
 * Contiene el par de credenciales (Access + Refresh) generados.
 */
public record LoginResult(
        String accessToken,
        String refreshToken
) {
}
