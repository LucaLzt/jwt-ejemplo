package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.RegisterCommand;
import com.ejemplos.jwt.application.ports.out.PasswordEncoderPort;
import com.ejemplos.jwt.domain.exception.personalized.EmailAlreadyExistsException;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private RegisterService registerService;

    @Test
    @DisplayName("Should register a user successfully when email is unique")
    void shouldRegisterSuccessfully() {
        // ARRANGE
        RegisterCommand registerCommand = new RegisterCommand("Test", "Demo", "test@demo.com", "testPassword");

        when(userRepository.existsByEmail("test@demo.com")).thenReturn(false);
        when(passwordEncoderPort.encode("testPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        User result = registerService.register(registerCommand);

        // ASSERT
        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionIfEmailExists() {
        // ARRANGE
        RegisterCommand registerCommand = new RegisterCommand("Test", "Demo", "test@demo.com", "testPassword");

        when(userRepository.existsByEmail("test@demo.com")).thenReturn(true);

        // ACT & ASSERT
        assertThrows(EmailAlreadyExistsException.class, () ->
                registerService.register(registerCommand)
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
