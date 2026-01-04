package com.ejemplos.jwt.infrastructure.security.user;

import com.ejemplos.jwt.application.ports.out.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura para el hashing de contraseñas.
 * <p>
 * Implementa el puerto {@link PasswordEncoderPort}, delegando la lógica real
 * al bean {@link PasswordEncoder} de Spring (generalmente BCrypt).
 * Así, el dominio no depende de la librería de Spring Security directamente.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class PasswordEncoderAdapter implements PasswordEncoderPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
