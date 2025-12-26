package com.ejemplos.jwt.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestRecoveryRequest(
        @NotBlank(message = "The password is required")
        @Size(min = 6, message = "The password must be at least 6 characters long")
        String email
) {
}
