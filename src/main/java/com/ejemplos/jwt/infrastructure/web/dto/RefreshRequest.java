package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitar la renovación del Access Token.
 * <p>
 * Se envía cuando el Access Token ha expirado (401) y el cliente intenta
 * obtener uno nuevo sin obligar al usuario a loguearse otra vez.
 * </p>
 */
public record RefreshRequest(
        @Schema(description = "El Refresh Token actual que se desea canjear por uno nuevo", example = "eyJhbGciOiJIUzI1NiIsIn...")
        @NotBlank(message = "The refresh token must not be blank")
        String refreshToken
) {
}
