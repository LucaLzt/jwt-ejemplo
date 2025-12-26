package com.ejemplos.jwt.application.ports.in;

public record LoginResult(
        String accessToken,
        String refreshToken
) {
}
