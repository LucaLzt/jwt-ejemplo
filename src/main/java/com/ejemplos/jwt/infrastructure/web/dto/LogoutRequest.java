package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de cierre de sesión.
 * <p>
 * Es necesario recibir el Refresh Token para poder invalidarlo en la base de datos
 * y evitar que se use para generar nuevos accesos.
 * </p>
 */
public record LogoutRequest(
        @Schema(description = "El Refresh Token que se desea invalidar explícitamente", example = "eyJhbGciOiJIUzI1NiIsIn...")
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
