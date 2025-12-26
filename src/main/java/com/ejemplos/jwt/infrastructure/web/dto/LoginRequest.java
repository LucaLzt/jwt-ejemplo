package com.ejemplos.jwt.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "The email address is required")
        @Email(message = "The email address must be valid")
        String email,

        @NotBlank(message = "The password is required")
        String password
) {
}
