package com.ejemplos.jwt.shared.email;

/**
 * Interfaz que define los servicios de envío de correos.
 */
public interface EmailService {

    void sendBasicEmail(String to, String subject, String body);
    void sendPasswordReset(String to, String link);

}
