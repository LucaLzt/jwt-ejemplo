package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para iniciar el flujo de "Olvidé mi contraseña".
 * <p>
 * Solo requiere el email del usuario para enviarle las instrucciones.
 * </p>
 */
public record RequestRecoveryRequest(
        @Schema(description = "El email de la cuenta a recuperar", example = "usuario@demo.com")
        @NotBlank(message = "The email is required")
        String email
) {
}
