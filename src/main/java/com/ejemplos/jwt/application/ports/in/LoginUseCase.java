package com.ejemplos.jwt.application.ports.in;

/**
 * Puerto de Entrada (Input Port) para la autenticación.
 */
public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
