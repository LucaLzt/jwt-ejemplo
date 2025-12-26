package com.ejemplos.jwt.application.ports.in;

public record RegisterCommand(
        String firstName,
        String lastName,
        String email,
        String password
) {
}
