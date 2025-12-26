package com.ejemplos.jwt.application.ports.in;

public record ResetPasswordCommand (
        String token,
        String newPassword
) {
}
