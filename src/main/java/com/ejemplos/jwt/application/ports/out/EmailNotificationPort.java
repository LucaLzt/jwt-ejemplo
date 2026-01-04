package com.ejemplos.jwt.application.ports.out;

/**
 * Puerto de Salida (Output Port) para servicios de notificación por correo.
 * <p>
 * Desacopla la lógica de negocio del proveedor de correos específico.
 * </p>
 */
public interface EmailNotificationPort {

    /**
     * Envía un correo con el enlace de recuperación de contraseña.
     *
     * @param to   Destinatario (email del usuario).
     * @param link Enlace completo (incluyendo el token) para resetear la clave.
     */
    void sendRecoveryEmail(String to, String link);

}
