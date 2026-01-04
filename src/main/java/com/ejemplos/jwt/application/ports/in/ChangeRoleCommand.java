package com.ejemplos.jwt.application.ports.in;

/**
 * Comando para la solicitud de cambio de rol.
 * Requiere el token actual para poder invalidar la sesión tras el cambio.
 */
public record ChangeRoleCommand(
        String email,
        String token
) {
}
