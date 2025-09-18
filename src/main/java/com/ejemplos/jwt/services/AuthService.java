package com.ejemplos.jwt.services;

import com.ejemplos.jwt.models.dtos.AuthDTO;
import com.ejemplos.jwt.models.dtos.LoginDTO;
import com.ejemplos.jwt.models.dtos.RegisterDTO;
import com.ejemplos.jwt.models.dtos.RefreshTokenRequestDTO;

/**
 * Interfaz que define los servicios de autenticación en el sistema.
 */
public interface AuthService {

    AuthDTO login(LoginDTO login) throws Exception;
    AuthDTO register(RegisterDTO register) throws Exception;
    AuthDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest) throws Exception;
    void logout(String refreshToken) throws Exception;

}
