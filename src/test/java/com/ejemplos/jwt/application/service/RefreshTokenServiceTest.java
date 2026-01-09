package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.RefreshTokenResult;
import com.ejemplos.jwt.application.ports.out.GeneratedToken;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import com.ejemplos.jwt.domain.exception.personalized.SecurityBreachException;
import com.ejemplos.jwt.domain.model.RefreshToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProviderPort jwtTokenProviderPort;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("Rotation Success: Should rotate tokens correctly")
    void shouldRotateTokens() {
        // ARRANGE
        String oldRefreshToken = "oldRefreshToken";
        Long userId = 1L;

        RefreshToken oldToken = RefreshToken.create(userId, oldRefreshToken, Instant.now().plusSeconds(3600));
        User user = new User(userId, "Luca", "Test", "luca@test.com", "pass", null, true, null, null);

        when(jwtTokenProviderPort.isRefreshTokenValid(oldRefreshToken)).thenReturn(true);
        when(refreshTokenRepository.findByToken(oldRefreshToken)).thenReturn(Optional.of(oldToken));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(jwtTokenProviderPort.generateAccessToken(user)).thenReturn("newAccessToken");
        when(jwtTokenProviderPort.generateRefreshToken(user)).thenReturn(new GeneratedToken("newRefreshToken", Instant.now().plusSeconds(3600)));

        // ACT
        RefreshTokenResult result = refreshTokenService.refresh(oldRefreshToken);

        // ASSERT
        assertEquals("newAccessToken", result.accessToken());
        assertEquals("newRefreshToken", result.refreshToken());

        assertTrue(oldToken.isRevoked());
        assertEquals("newRefreshToken", oldToken.getReplacedBy());

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Security Breach: Should revoke all tokens if reused")
    void shouldDetectTokenReuse() {
        // ARRANGE
        String stolenRefreshToken = "stolenRefreshToken";
        Long userId = 1L;

        RefreshToken stolenToken = RefreshToken.create(userId, stolenRefreshToken, Instant.now().plusSeconds(3600));
        stolenToken.revoke();

        when(jwtTokenProviderPort.isRefreshTokenValid(stolenRefreshToken)).thenReturn(true);
        when(refreshTokenRepository.findByToken(stolenRefreshToken)).thenReturn(Optional.of(stolenToken));

        // ACT & ASSERT
        assertThrows(SecurityBreachException.class, () ->
                refreshTokenService.refresh(stolenRefreshToken)
        );

        verify(refreshTokenRepository).revokeAllTokens(userId);
    }

    @Test
    @DisplayName("Expiration: Should throw exception if expired")
    void shouldThrowIfExpired() {
        // ARRANGE
        String expiredRefreshToken = "expiredRefreshToken";
        RefreshToken expiredToken = RefreshToken.create(1L, expiredRefreshToken, Instant.now().minusSeconds(10));

        when(jwtTokenProviderPort.isRefreshTokenValid(expiredRefreshToken)).thenReturn(true);
        when(refreshTokenRepository.findByToken(expiredRefreshToken)).thenReturn(Optional.of(expiredToken));

        // ACT & ASSERT
        assertThrows(InvalidTokenException.class, () ->
                refreshTokenService.refresh(expiredRefreshToken)
        );
    }
}
