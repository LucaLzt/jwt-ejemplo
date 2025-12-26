package com.ejemplos.jwt.application.ports.in;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
