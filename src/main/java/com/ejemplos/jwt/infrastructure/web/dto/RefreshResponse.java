package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta tras la rotación de tokens.
 * <p>
 * Devuelve siempre un par nuevo, ya que aplicamos "Refresh Token Rotation"
 * (el refresh token anterior muere al ser usado).
 * </p>
 */
public record RefreshResponse(
        @Schema(description = "Nuevo Token de Acceso generado", example = "eyJhbGciOiJIUzI1NiIsIn...")
        String accessToken,

        @Schema(description = "Nuevo Token de Refresco (Rotación de token aplicada)", example = "eyJhbGciOiJIUzI1NiIsIn...")
        String refreshToken
) {
}
