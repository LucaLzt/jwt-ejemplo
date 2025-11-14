package com.ejemplos.jwt.features.auth.application.service;

import com.ejemplos.jwt.features.auth.application.dto.AuthDTO;
import com.ejemplos.jwt.features.auth.application.dto.LoginDTO;
import com.ejemplos.jwt.features.auth.application.dto.RegisterDTO;

/**
 * Interfaz que define los servicios de autenticación en el sistema.
 */
public interface AuthService {

    AuthDTO login(LoginDTO login) throws Exception;
    AuthDTO register(RegisterDTO register) throws Exception;
    AuthDTO refresh(String providedRefreshToken);
    void logout(String userEmail);
    void logoutByAccessToken(String bearerToken);

}
