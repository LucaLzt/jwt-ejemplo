package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.LogoutCommand;
import com.ejemplos.jwt.application.ports.in.LogoutUseCase;
import com.ejemplos.jwt.domain.model.RevokedToken;
import com.ejemplos.jwt.domain.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        RevokedToken revokedToken = RevokedToken.revoke(
                command.jti(),
                command.email(),
                "User logout",
                command.expiration()
        );

        revokedTokenRepository.save(revokedToken);
    }
}
