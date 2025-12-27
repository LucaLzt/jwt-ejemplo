package com.ejemplos.jwt.application.ports.in;

import java.time.Instant;

public record LogoutCommand(
        String jti,
        String email,
        Instant expiration,
        String refreshToken
) {
}
