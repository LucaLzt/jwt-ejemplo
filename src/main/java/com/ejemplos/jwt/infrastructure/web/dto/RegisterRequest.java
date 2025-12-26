package com.ejemplos.jwt.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "The first name is required")
        String firstName,

        @NotBlank(message = "The last name is required")
        String lastName,

        @NotBlank(message = "The email address is required")
        @Email(message = "The email address must be valid")
        String email,

        @NotBlank(message = "The password is required")
        @Size(min = 6, message = "The password must be at least 6 characters long")
        String password
) {
}
