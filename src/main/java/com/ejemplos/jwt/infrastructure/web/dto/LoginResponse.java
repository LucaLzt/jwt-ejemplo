package com.ejemplos.jwt.infrastructure.web.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
