package com.ejemplos.jwt.services;

/**
 * Interfaz que define los servicios de recuperación de contraseña del sistema.
 */
public interface PasswordRecoveryService {

    void requestRecovery(String email, String appBaseUrl);
    void confirmReset(String token, String newPassword, String repeatNewPassword);

}
