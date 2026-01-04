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

/**
 * Servicio que gestiona el cambio de roles de usuario.
 * <p>
 * Este servicio demuestra un principio de seguridad importante:
 * <strong>Cuando cambian los privilegios, se debe invalidar la sesión.</strong>
 * De lo contrario, el usuario seguiría navegando con un token que tiene el rol antiguo
 * (claims obsoletos) hasta que expire.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ChangeRoleService implements ChangeRoleUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProviderPort jwtTokenProviderPort;
    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    @Transactional
    public void changeRole(ChangeRoleCommand command) {
        // 1. Buscar y actualizar usuario (Lógica de Dominio)
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + command.email()));

        user.toggleRole();          // Cambia de ADMIN a CLIENT o viceversa
        userRepository.save(user);

        // 2. Seguridad: Revocar TODOS los Refresh Tokens
        // Obliga al usuario a loguearse de nuevo eventualmente si intenta renovar
        refreshTokenRepository.revokeAllTokens(user.getId());

        // 3. Seguridad: Matar la sesión actual (Blacklist)
        // Como el Access Token actual dice que tiene el rol "viejo",
        // lo invalidamos explícitamente para forzarlo a salir YA.
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
