package com.ejemplos.jwt.application.ports.in;

public record ChangeRoleCommand(
        String email,
        String token
) {
}
