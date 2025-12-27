package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @Schema(description = "El token UUID recibido por correo", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "The refresh token must not be blank")
        String token,

        @Schema(description = "La nueva contraseña que desea establecer", example = "NuevaPassword123")
        @NotBlank(message = "The password is required")
        @Size(min = 6, message = "The password must be at least 6 characters long")
        String newPassword
) {
}
