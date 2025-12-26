package com.ejemplos.jwt.application.ports.out;

import java.time.Instant;

public record GeneratedToken(
        String token,
        Instant expiresAt
) {
}
