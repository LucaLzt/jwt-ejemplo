package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.LogoutCommand;
import com.ejemplos.jwt.application.ports.in.LogoutUseCase;
import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import com.ejemplos.jwt.domain.model.RefreshToken;
import com.ejemplos.jwt.domain.model.RevokedToken;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio para el cierre de sesión seguro.
 * <p>
 * Implementa una estrategia de "Defensa en Profundidad":
 * 1. Invalida el Refresh Token en la BD (para que no pueda sacar más tokens).
 * 2. Invalida el Access Token actual en una Blacklist (para que no pueda usar el tiempo que le queda).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RevokedTokenRepository revokedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        // 1. Blacklist del Access Token (Seguridad Inmediata)
        // Guardamos el JTI para que el filtro de seguridad lo rechace en futuros requests
        RevokedToken revokedToken = RevokedToken.revoke(
                command.jti(),
                command.email(),
                "User logout",
                command.expiration()
        );
        revokedTokenRepository.save(revokedToken);

        // 2. Revocación del Refresh Token (Seguridad a Largo Plazo)
        // Marcamos el token de base de datos como revocado
        RefreshToken storedToken = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid or does not exist"));

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
    }
}
