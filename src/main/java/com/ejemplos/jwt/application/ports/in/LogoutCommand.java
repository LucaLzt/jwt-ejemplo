package com.ejemplos.jwt.application.ports.in;

import java.time.Instant;

/**
 * Datos requeridos para cerrar la sesión de forma segura.
 * Incluye información del token actual para poder invalidarlo (Blacklist).
 */
public record LogoutCommand(
        String jti,
        String email,
        Instant expiration,
        String refreshToken
) {
}
