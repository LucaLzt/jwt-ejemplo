package com.ejemplos.jwt.application.ports.out;

import java.time.Instant;

/**
 * DTO que representa un token generado por el proveedor de seguridad.
 * Transporta el String del token y su fecha exacta de expiración.
 */
public record GeneratedToken(
        String token,
        Instant expiresAt
) {
}
