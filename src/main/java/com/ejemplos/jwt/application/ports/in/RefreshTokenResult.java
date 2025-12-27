package com.ejemplos.jwt.application.ports.in;

public record RefreshTokenResult(
        String accessToken,
        String refreshToken
) {
}
