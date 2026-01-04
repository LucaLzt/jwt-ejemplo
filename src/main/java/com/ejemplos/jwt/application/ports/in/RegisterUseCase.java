package com.ejemplos.jwt.application.ports.in;

import com.ejemplos.jwt.domain.model.User;

/**
 * Puerto de Entrada (Input Port) para el registro de usuarios.
 * Define el contrato de la operación de crear una nueva cuenta.
 */
public interface RegisterUseCase {
    User register(RegisterCommand command);
}
