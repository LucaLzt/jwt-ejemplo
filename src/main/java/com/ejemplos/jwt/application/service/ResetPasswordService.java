package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.ResetPasswordCommand;
import com.ejemplos.jwt.application.ports.in.ResetPasswordUseCase;
import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RecoveryTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final RecoveryTokenRepository recoveryTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        RecoveryToken recoveryToken = recoveryTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (!recoveryToken.isValid()) {
            throw new IllegalArgumentException("Token expired or already used");
        }

        User user = userRepository.findByEmail(recoveryToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));;
        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        recoveryToken.markAsUsed();;
        recoveryTokenRepository.save(recoveryToken);
    }
}
