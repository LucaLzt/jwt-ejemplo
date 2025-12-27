package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshResponse(
        @Schema(description = "Nuevo Token de Acceso generado", example = "eyJhbGciOiJIUzI1NiIsIn...")
        String accessToken,

        @Schema(description = "Nuevo Token de Refresco (Rotación de token aplicada)", example = "eyJhbGciOiJIUzI1NiIsIn...")
        String refreshToken
) {
}
