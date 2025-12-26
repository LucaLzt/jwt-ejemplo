package com.ejemplos.jwt.application.ports.in;

public interface RefreshTokenUseCase {
    String refresh(String refreshTokenValue);
}
