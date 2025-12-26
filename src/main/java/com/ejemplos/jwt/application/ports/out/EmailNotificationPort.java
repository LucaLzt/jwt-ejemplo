package com.ejemplos.jwt.application.ports.out;

/**
 * Interfaz que define los servicios de envío de correos.
 */
public interface EmailNotificationPort {

    void sendRecoveryEmail(String to, String link);

}
