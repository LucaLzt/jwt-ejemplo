package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.ChangeRoleCommand;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.domain.enums.UserRole;
import com.ejemplos.jwt.domain.model.RevokedToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.RevokedTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeRoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProviderPort jwtTokenProviderPort;

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @InjectMocks
    private ChangeRoleService changeRoleService;

    @Test
    @DisplayName("Should toggle role and invalidate sessions")
    void shouldChangeRole() {
        // ARRANGE
        String email = "test@demo.com";
        String currentToken = "currentToken";

        User user = User.create("Test", "Demo", "test@demo.com", "testPassword");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProviderPort.getJtiFromToken(currentToken)).thenReturn("jti_1");
        when(jwtTokenProviderPort.getExpirationFromToken(currentToken)).thenReturn(Instant.now());

        // ACT
        changeRoleService.changeRole(new ChangeRoleCommand(email, currentToken));

        // ASSERT
        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository).save(user);

        verify(refreshTokenRepository).revokeAllTokens(user.getId());

        verify(revokedTokenRepository).save(any(RevokedToken.class));
    }
}
