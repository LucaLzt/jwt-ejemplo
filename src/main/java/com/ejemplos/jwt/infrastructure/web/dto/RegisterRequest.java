package com.ejemplos.jwt.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Schema(description = "Nombre de pila del usuario", example = "Jon")
        @NotBlank(message = "The first name is required")
        String firstName,

        @Schema(description = "Apellido del usuario", example = "Doe")
        @NotBlank(message = "The last name is required")
        String lastName,

        @Schema(description = "Correo electrónico único para la cuenta", example = "usuario@demo.com")
        @NotBlank(message = "The email address is required")
        @Email(message = "The email address must be valid")
        String email,

        @Schema(description = "Contraseña segura (mínimo 6 caracteres)", example = "123456")
        @NotBlank(message = "The password is required")
        @Size(min = 6, message = "The password must be at least 6 characters long")
        String password
) {
}
