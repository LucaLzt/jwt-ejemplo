package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.LogoutCommand;
import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import com.ejemplos.jwt.domain.model.RefreshToken;
import com.ejemplos.jwt.domain.model.RevokedToken;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.RevokedTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    @DisplayName("Should logout successfully (revoke access and refresh tokens)")
    void shouldLogoutSuccessfully() {
        // ARRANGE
        String refreshToken = "refreshToken";
        LogoutCommand command = new LogoutCommand("jti_123", "test@demo.com", Instant.now().plusSeconds(300), refreshToken);

        RefreshToken existingToken = RefreshToken.create(1L, refreshToken, Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(existingToken));

        // ACT
        logoutService.logout(command);

        // ASSERT
        verify(revokedTokenRepository).save(any(RevokedToken.class));

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception if refresh token does not exist")
    void shouldThrowIfRefreshTokenNotFound() {
        // ARRANGE
        LogoutCommand command = new LogoutCommand("jti", "mail", Instant.now(), "invalid_token");

        when(refreshTokenRepository.findByToken("invalid_token")).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(InvalidTokenException.class, () ->
                logoutService.logout(command)
        );
    }
}
