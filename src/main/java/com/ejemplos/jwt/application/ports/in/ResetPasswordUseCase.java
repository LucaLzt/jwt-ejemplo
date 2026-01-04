package com.ejemplos.jwt.application.ports.in;

/**
 * Puerto de Entrada (Input Port) para procesar el cambio de contraseña.
 * Valida el token de recuperación y actualiza las credenciales del usuario.
 */
public interface ResetPasswordUseCase {

    void resetPassword(ResetPasswordCommand command);

}
