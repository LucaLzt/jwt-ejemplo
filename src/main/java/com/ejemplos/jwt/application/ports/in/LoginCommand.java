package com.ejemplos.jwt.application.ports.in;

/**
 * Comando con las credenciales para iniciar sesión.
 */
public record LoginCommand(
        String email,
        String password
) {
}
