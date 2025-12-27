package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @Schema(description = "El Refresh Token que se desea invalidar explícitamente", example = "eyJhbGciOiJIUzI1NiIsIn...")
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
