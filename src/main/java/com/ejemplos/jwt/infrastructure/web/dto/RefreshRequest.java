package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @Schema(description = "El Refresh Token actual que se desea canjear por uno nuevo", example = "eyJhbGciOiJIUzI1NiIsIn...")
        @NotBlank(message = "The refresh token must not be blank")
        String refreshToken
) {
}
