package com.ejemplos.jwt.application.ports.in;

public record LoginCommand(
        String email,
        String password
) {
}
