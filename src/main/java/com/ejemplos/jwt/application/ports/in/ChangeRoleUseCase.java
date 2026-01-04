package com.ejemplos.jwt.application.ports.in;

/**
 * Puerto de Entrada (Input Port) para la gestión de roles de usuario.
 */
public interface ChangeRoleUseCase {
    void changeRole(ChangeRoleCommand command);
}
