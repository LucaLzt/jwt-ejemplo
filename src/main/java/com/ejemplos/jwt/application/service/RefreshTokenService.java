package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.RefreshTokenResult;
import com.ejemplos.jwt.application.ports.in.RefreshTokenUseCase;
import com.ejemplos.jwt.application.ports.out.GeneratedToken;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import com.ejemplos.jwt.domain.exception.personalized.SecurityBreachException;
import com.ejemplos.jwt.domain.exception.personalized.UserNotFoundException;
import com.ejemplos.jwt.domain.model.RefreshToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProviderPort jwtTokenProviderPort;

    @Override
    @Transactional(noRollbackFor = SecurityBreachException.class)
    public RefreshTokenResult refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            refreshTokenRepository.revokeAllTokens(refreshToken.getUserId());
            throw new SecurityBreachException("Token reuse detected. Session closed for security reasons.");
        }

        if (refreshToken.isExpired()) {
            throw new InvalidTokenException("The refresh token is expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found for the provided token"));

        GeneratedToken refreshTokenData = jwtTokenProviderPort.generateRefreshToken(user);
        String newAccessToken = jwtTokenProviderPort.generateAccessToken(user);

        RefreshToken newRefreshToken = RefreshToken.create(
                user.getId(),
                refreshTokenData.token(),
                refreshTokenData.expiresAt()
        );

        refreshToken.revoke();
        refreshToken.setReplacedBy(newRefreshToken.getToken());
        refreshTokenRepository.save(refreshToken);

        refreshTokenRepository.save(newRefreshToken);

        return new RefreshTokenResult(newAccessToken, newRefreshToken.getToken());
    }
}
