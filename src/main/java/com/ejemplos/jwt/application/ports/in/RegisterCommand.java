package com.ejemplos.jwt.application.ports.in;

/**
 * Comando que encapsula los datos necesarios para registrar un usuario.
 * <p>
 * Actúa como un DTO (Data Transfer Object) de entrada agnóstico a la web.
 * </p>
 */
public record RegisterCommand(
        String firstName,
        String lastName,
        String email,
        String password
) {
}
