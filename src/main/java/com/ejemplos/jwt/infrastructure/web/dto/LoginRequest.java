package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para iniciar sesión.
 * <p>
 * Contiene las credenciales (usuario y contraseña) enviadas en el endpoint POST /auth/login.
 * </p>
 */
public record LoginRequest(
        @Schema(description = "Correo electrónico del usuario registrado", example = "usuario@demo.com")
        @NotBlank(message = "The email address is required")
        @Email(message = "The email address must be valid")
        String email,

        @Schema(description = "Contraseña del usuario", example = "123456")
        @NotBlank(message = "The password is required")
        String password
) {
}
