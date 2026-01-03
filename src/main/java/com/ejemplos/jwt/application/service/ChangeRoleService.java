package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.ChangeRoleCommand;
import com.ejemplos.jwt.application.ports.in.ChangeRoleUseCase;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.domain.exception.personalized.UserNotFoundException;
import com.ejemplos.jwt.domain.model.RevokedToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.RevokedTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangeRoleService implements ChangeRoleUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProviderPort jwtTokenProviderPort;
    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    public void changeRole(ChangeRoleCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + command.email()));

        user.toggleRole();
        userRepository.save(user);

        refreshTokenRepository.revokeAllTokens(user.getId());

        String jti = jwtTokenProviderPort.getJtiFromToken(command.token());
        Instant expiresAt = jwtTokenProviderPort.getExpirationFromToken(command.token());

        RevokedToken revokedToken = RevokedToken.revoke(
                jti,
                command.email(),
                "Role Change - Session Invalidated",
                expiresAt
        );

        revokedTokenRepository.save(revokedToken);
    }
}
