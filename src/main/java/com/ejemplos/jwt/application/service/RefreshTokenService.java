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

/**
 * Servicio encargado de la renovación de sesiones mediante Refresh Tokens.
 * <p>
 * Implementa el patrón de <strong>Rotación de Refresh Tokens</strong> para máxima seguridad.
 * </p>
 * <h3>¿Cómo funciona la Rotación?</h3>
 * <ol>
 * <li>El cliente envía un Refresh Token.</li>
 * <li>Si es válido, se le entrega uno NUEVO y el anterior se marca como "reemplazado".</li>
 * <li>Si el cliente (o un hacker) intenta usar el token viejo de nuevo, el sistema detecta el reuso
 * y asume que ha habido un robo, cerrando todas las sesiones del usuario.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProviderPort jwtTokenProviderPort;

    /**
     * Procesa la solicitud de refresco.
     * <p>
     * <strong>Nota sobre Transaccionalidad:</strong><br>
     * Usamos {@code noRollbackFor = SecurityBreachException.class} porque si detectamos un robo,
     * lanzamos esa excepción PERO queremos que la base de datos SÍ guarde la revocación de todos los tokens.
     * Si hiciéramos rollback, los tokens seguirían vivos y el hacker podría seguir intentando.
     * </p>
     */
    @Override
    @Transactional(noRollbackFor = SecurityBreachException.class)
    public RefreshTokenResult refresh(String refreshTokenValue) {

        // 1. Validación de formato y firma criptográfica (Sin ir a BD aún)
        if (!jwtTokenProviderPort.isRefreshTokenValid(refreshTokenValue)) {
            throw new InvalidTokenException("Invalid refresh token format or signature");
        }

        // 2. Buscamos el token en la base de datos
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // DETECCIÓN DE ROBO (Token Reuse Detection)
        // Si el token ya fue usado (revoked) y alguien lo envía de nuevo salta la alarma.
        // Significa que el usuario legítimo ya lo rotó, y quien envía esto ahora tiene una copia vieja.
        if (refreshToken.isRevoked()) {
            // Medida drástica: Revocar TODO acceso a este usuario.
            refreshTokenRepository.revokeAllTokens(refreshToken.getUserId());
            throw new SecurityBreachException("Token reuse detected. Session closed for security reasons.");
        }

        // 3. Validar expiración temporal
        if (refreshToken.isExpired()) {
            throw new InvalidTokenException("The refresh token is expired");
        }

        // 4. Recuperar al usuario dueño de la sesión
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found for the provided token"));

        // 5. Creamos la entidad del NUEVO token
        GeneratedToken refreshTokenData = jwtTokenProviderPort.generateRefreshToken(user);
        String newAccessToken = jwtTokenProviderPort.generateAccessToken(user);

        // 5. Creamos la entidad del NUEVO token
        RefreshToken newRefreshToken = RefreshToken.create(
                user.getId(),
                refreshTokenData.token(),
                refreshTokenData.expiresAt()
        );

        // 6. Invalidamos el VIEJO y apuntamos al nuevo (Cadena de custodia)
        refreshToken.revoke();
        refreshToken.setReplacedBy(newRefreshToken.getToken());

        // 7. Guardamos ambos cambios
        refreshTokenRepository.save(refreshToken);      // Viejo actualizado
        refreshTokenRepository.save(newRefreshToken);   // Nuevo creado

        return new RefreshTokenResult(newAccessToken, newRefreshToken.getToken());
    }
}
