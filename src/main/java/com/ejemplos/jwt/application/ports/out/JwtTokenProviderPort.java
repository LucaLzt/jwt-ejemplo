package com.ejemplos.jwt.application.ports.out;

import com.ejemplos.jwt.domain.model.User;
import org.springframework.security.core.Authentication;

import java.time.Instant;

/**
 * Puerto de Salida (Output Port) para operaciones criptográficas con JWT.
 * <p>
 * Centraliza la generación, validación y extracción de datos de los tokens.
 * </p>
 */
public interface JwtTokenProviderPort {

    /** Genera un Access Token de corta duración. */
    String generateAccessToken(User user);

    /** Genera un Refresh Token de larga duración. */
    GeneratedToken generateRefreshToken(User user);

    /** Valida matemáticamente y por fecha un Access Token. */
    boolean isAccessTokenValid(String token);

    /** Valida matemáticamente y por fecha un Refresh Token. */
    boolean isRefreshTokenValid(String token);

    /** Convierte un token válido en un objeto de autenticación de Spring Security. */
    Authentication getAuthentication(String token);

    /** Extrae el email (subject) del token. */
    String getUsernameFromToken(String token);

    /** Extrae el ID único (JTI) del token. */
    String getJtiFromToken(String token);

    /** Extrae la fecha de expiración del token. */
    Instant getExpirationFromToken(String token);
}
