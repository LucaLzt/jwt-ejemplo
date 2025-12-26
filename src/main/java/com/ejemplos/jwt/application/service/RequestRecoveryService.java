package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.RequestRecoveryUseCase;
import com.ejemplos.jwt.application.ports.out.EmailNotificationPort;
import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.domain.repository.RecoveryTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestRecoveryService implements RequestRecoveryUseCase {

    private final UserRepository userRepository;
    private final RecoveryTokenRepository recoveryTokenRepository;
    private final EmailNotificationPort emailNotificationPort;

    @Override
    public void requestRecovery(String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            System.out.print("ERROR -> NO SE ENCONTRÓ EL USUARIO");
            return;
        }

        String tokenString = UUID.randomUUID().toString();
        RecoveryToken recoveryToken = RecoveryToken.create(
                email,
                tokenString,
                15*60
        );

        recoveryTokenRepository.save(recoveryToken);

        String link = "https://miapp.com/recover?token=" + tokenString;
        emailNotificationPort.sendRecoveryEmail(email, link);
    }
}
