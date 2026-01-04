package com.ejemplos.jwt.application.ports.in;

/**
 * Puerto de Entrada (Input Port) para cerrar sesión.
 * Se encarga de coordinar la invalidación de tokens.
 */
public interface LogoutUseCase {
    void logout(LogoutCommand command);
}
