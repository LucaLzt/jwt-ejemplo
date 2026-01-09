package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.ResetPasswordCommand;
import com.ejemplos.jwt.application.ports.out.PasswordEncoderPort;
import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RecoveryTokenRepository;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    @Mock
    private RecoveryTokenRepository recoveryTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private ResetPasswordService resetPasswordService;

    @Test
    @DisplayName("Should reset password and revoke previous sessions")
    void shouldResetPassword() {
        // ARRANGE
        String tokenValue = "tokenValue";
        String email = "test@demo.com";
        String newPassword = "newPassword";

        RecoveryToken recoveryToken = new RecoveryToken(1L, tokenValue, email, Instant.now().plusSeconds(600), false);
        User user = new User(1L, "Luca", "Test", email, "oldPass", null, true, null, null);

        when(recoveryTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(recoveryToken));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.encode(newPassword)).thenReturn("newHashedPassword");

        // ACT
        resetPasswordService.resetPassword(new ResetPasswordCommand(tokenValue, newPassword));

        // ASSERT
        verify(userRepository).save(argThat(u -> u.getPassword().equals("newHashedPassword")));

        verify(recoveryTokenRepository).save(argThat(t -> t.isUsed()));

        verify(refreshTokenRepository).revokeAllTokens(user.getId());
    }

    @Test
    @DisplayName("Should throw exception if token is expired or used")
    void shouldThrowIfTokenInvalid() {
        // ARRANGE
        String tokenValue = "tokenValue";
        String newPassword = "newPassword";

        RecoveryToken invalidToken = new RecoveryToken(1L, tokenValue, "test@demo.com", Instant.now().plusSeconds(600), true);

        when(recoveryTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(invalidToken));

        ResetPasswordCommand resetPasswordCommand = new ResetPasswordCommand(tokenValue, newPassword);

        // ACT & ASSERT
        assertThrows(InvalidTokenException.class, () ->
                resetPasswordService.resetPassword(resetPasswordCommand)
        );

        verify(userRepository, never()).save(any());
    }
}