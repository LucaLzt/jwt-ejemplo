package com.ejemplos.jwt.application.ports.in;

/**
 * Puerto de Entrada (Input Port) para solicitar la recuperación de contraseña.
 * <p>
 * Inicia el proceso generando un token temporal y enviándolo al correo del usuario.
 * </p>
 */
public interface RequestRecoveryUseCase {

    /**
     * Inicia el flujo de recuperación.
     * @param email El correo electrónico del usuario que quiere recuperar su cuenta.
     */
    void requestRecovery(String email);

}
