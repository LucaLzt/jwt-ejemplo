package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.LogoutCommand;
import com.ejemplos.jwt.application.ports.in.LogoutUseCase;
import com.ejemplos.jwt.domain.model.RefreshToken;
import com.ejemplos.jwt.domain.model.RevokedToken;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RevokedTokenRepository revokedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

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

        Optional<RefreshToken> storedToken = refreshTokenRepository.findByToken(command.refreshToken());

        if (storedToken.isPresent()) {
            RefreshToken refreshToken = storedToken.get();
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
        }
    }
}
