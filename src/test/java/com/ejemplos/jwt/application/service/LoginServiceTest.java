package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.LoginCommand;
import com.ejemplos.jwt.application.ports.in.LoginResult;
import com.ejemplos.jwt.application.ports.out.GeneratedToken;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.application.ports.out.PasswordEncoderPort;
import com.ejemplos.jwt.domain.exception.personalized.InvalidCredentialsException;
import com.ejemplos.jwt.domain.exception.personalized.UserNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProviderPort jwtTokenProviderPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("Login Success: Should return Access and Refresh tokens")
    void shouldLoginSuccessfully() {
        // ARRANGE
        String email = "test@demo.com";
        String rawPass = "testPassword";
        String encodedPass = "encodedTestPassword";

        User userWithId = new User(1L, "Test", "Demo", email, encodedPass, null, true, null, null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userWithId));
        when(passwordEncoderPort.matches(rawPass, encodedPass)).thenReturn(true);

        when(jwtTokenProviderPort.generateAccessToken(userWithId)).thenReturn("accessToken");
        when(jwtTokenProviderPort.generateRefreshToken(userWithId)).thenReturn(new GeneratedToken("refreshToken", Instant.now()));

        // ACT
        LoginResult result = loginService.login(new LoginCommand(email, rawPass));

        // ASSERT
        assertNotNull(result);
        assertEquals("accessToken", result.accessToken());
        assertEquals("refreshToken", result.refreshToken());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Login Fail: Should throw exception on wrong password")
    void shouldThrowExceptionOnWrongPassword() {
        // ARRANGE
        String email = "test@demo.com";
        User user = User.create("Test", "Demo", email, "correctPass");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches("wrongPass", "correctPass")).thenReturn(false);

        LoginCommand loginCommand = new LoginCommand(email, "wrongPass");

        // ACT
        assertThrows(InvalidCredentialsException.class, () ->
                loginService.login(loginCommand)
        );

        // ASSERT
        verify(jwtTokenProviderPort, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Login Fail: Should throw exception if user not found")
    void shouldThrowExceptionIfUserNotFound() {
        // ARRANGE
        when(userRepository.findByEmail("test@demo.com")).thenReturn(Optional.empty());

        LoginCommand loginCommand = new LoginCommand("test@demo.com", "testPassword");

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class, () ->
                loginService.login(loginCommand)
        );
    }
}
