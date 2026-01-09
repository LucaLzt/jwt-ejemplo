package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.out.EmailNotificationPort;
import com.ejemplos.jwt.domain.exception.personalized.UserNotFoundException;
import com.ejemplos.jwt.domain.model.RecoveryToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RecoveryTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestRecoveryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecoveryTokenRepository recoveryTokenRepository;

    @Mock
    private EmailNotificationPort emailNotificationPort;

    @InjectMocks
    private RequestRecoveryService requestRecoveryService;

    @Test
    @DisplayName("Should create token and send email if user exists")
    void shouldSendRecoveryEmail() {
        // ARRANGE
        String email = "test@demo.com";
        User user = User.create("Test", "Demo", email, "testPassword");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // ACT
        requestRecoveryService.requestRecovery(email);

        // ASSERT
        verify(recoveryTokenRepository).save(any(RecoveryToken.class));

        verify(emailNotificationPort).sendRecoveryEmail(eq(email), anyString());
    }

    @Test
    @DisplayName("Should throw exception if user does not exist")
    void shouldThrowIfUserNotFound() {
        // ARRANGE
        String email = "test@demo.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class, () ->
                requestRecoveryService.requestRecovery(email)
        );

        verify(emailNotificationPort, never()).sendRecoveryEmail(eq(email), anyString());
    }
}
