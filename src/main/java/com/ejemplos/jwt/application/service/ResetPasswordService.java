package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.ResetPasswordCommand;
import com.ejemplos.jwt.application.ports.in.ResetPasswordUseCase;
import com.ejemplos.jwt.application.ports.out.PasswordEncoderPort;
import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import com.ejemplos.jwt.domain.exception.personalized.UserNotFoundException;
import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RecoveryTokenRepository;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que finaliza el cambio de contraseña.
 * <p>
 * Este servicio implementa una medida de seguridad crítica: <strong>Session Invalidation</strong>.
 * Si un usuario cambia su contraseña (quizás porque fue hackeado), debemos asumir que sus
 * sesiones activas ya no son confiables.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final RecoveryTokenRepository recoveryTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public void resetPassword(ResetPasswordCommand command) {
        // 1. Validar existencia del token de recuperación
        RecoveryToken recoveryToken = recoveryTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        // 2. Validar estado del token (No usado y no expirado)
        if (!recoveryToken.isValid()) {
            throw new InvalidTokenException("Token expired or already used");
        }

        // 3. Obtener al usuario asociado
        User user = userRepository.findByEmail(recoveryToken.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found for the provided token"));

        // 4. Actualizar la contraseña (Hasheada)
        user.setPassword(passwordEncoderPort.encode(command.newPassword()));
        userRepository.save(user);

        // Si la contraseña cambió, todas las sesiones abiertas (Refresh Tokens)
        // deben morir. Esto expulsa inmediatamente a cualquier atacante que
        // pudiera tener una sesión activa en otro dispositivo.
        refreshTokenRepository.revokeAllTokens(user.getId());

        // 5. Consumir el token de recuperación (One-Time Use)
        // Evita ataques de replay donde se intente usar el mismo link dos veces.
        recoveryToken.markAsUsed();
        recoveryTokenRepository.save(recoveryToken);
    }
}