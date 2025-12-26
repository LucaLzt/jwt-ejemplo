package com.ejemplos.jwt.infrastructure.web.dto;

public record ResetPasswordRequest(
        String token,
        String newPassword
) {
}
