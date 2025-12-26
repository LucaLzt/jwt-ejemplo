package com.ejemplos.jwt.application.ports.in;

public interface RefreshTokenUseCase {
    RefreshTokenResult refresh(String refreshTokenValue);
}
