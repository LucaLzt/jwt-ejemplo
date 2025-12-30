package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.ResetPasswordCommand;
import com.ejemplos.jwt.application.ports.in.ResetPasswordUseCase;
import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import com.ejemplos.jwt.domain.exception.personalized.UserNotFoundException;
import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RecoveryTokenRepository;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ResetPasswordService implements ResetPasswordUseCase {

    private final RecoveryTokenRepository recoveryTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        RecoveryToken recoveryToken = recoveryTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (!recoveryToken.isValid()) {
            throw new InvalidTokenException("Token expired or already used");
        }

        User user = userRepository.findByEmail(recoveryToken.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found for the provided token"));
        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllTokens(user.getId());

        recoveryToken.markAsUsed();
        recoveryTokenRepository.save(recoveryToken);
    }
}