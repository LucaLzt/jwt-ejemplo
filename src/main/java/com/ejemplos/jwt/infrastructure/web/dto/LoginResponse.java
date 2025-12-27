package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "Token de acceso JWT (corta duración)", example = "eyJhbGciOiJIUzI1NiIsIn...")
        String accessToken,

        @Schema(description = "Token de refresco (larga duración) para renovar sesión", example = "eyJhbGciOiJIUzI1NiIsIn...")
        String refreshToken
) {
}
