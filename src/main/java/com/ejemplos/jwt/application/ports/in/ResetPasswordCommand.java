package com.ejemplos.jwt.application.ports.in;

/**
 * Comando que encapsula los datos para finalizar el cambio de contraseña.
 * Contiene el token de seguridad recibido por correo y la nueva clave deseada.
 */
public record ResetPasswordCommand (
        String token,
        String newPassword
) {
}
