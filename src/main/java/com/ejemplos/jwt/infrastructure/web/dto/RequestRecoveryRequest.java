package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RequestRecoveryRequest(
        @Schema(description = "El email de la cuenta a recuperar", example = "usuario@demo.com")
        @NotBlank(message = "The email is required")
        String email
) {
}
