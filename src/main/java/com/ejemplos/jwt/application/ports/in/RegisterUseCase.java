package com.ejemplos.jwt.application.ports.in;

import com.ejemplos.jwt.domain.model.User;

public interface RegisterUseCase {
    User register(RegisterCommand command);
}
