package com.ejemplos.jwt.infrastructure.security.jwt;

import com.ejemplos.jwt.application.ports.out.GeneratedToken;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RevokedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Implementación concreta del proveedor de JWT usando la librería 'jjwt'.
 * <p>
 * Responsabilidades:
 * 1. Generar tokens firmados (HMAC-SHA).
 * 2. Parsear y validar tokens entrantes.
 * 3. Extraer claims (datos) del token.
 * 4. Convertir un token válido en un objeto Authentication de Spring.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProviderAdapter implements JwtTokenProviderPort {

    private final JwtProperties jwtProperties;
    private final RevokedTokenRepository revokedTokenRepository;

    private SecretKey secretKey;

    /**
     * Inicializa la llave criptográfica al arrancar la aplicación.
     * <p>
     * Convierte el 'secret-key' de texto plano (definido en application.yml)
     * en una estructura de llave {@link SecretKey} optimizada para el algoritmo HMAC-SHA.
     * </p>
     */
    @PostConstruct
    private void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    // =================================================================================
    // GENERACIÓN DE TOKENS
    // =================================================================================

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.getAccessTokenExpirationSeconds());

        // El Access Token lleva datos útiles (Claims) para evitar ir a la BD en cada request
        return Jwts.builder()
                .subject(user.getEmail())
                .issuer("jwt-ejemplo")
                .id(UUID.randomUUID().toString())           // JTI: ID único para poder revocarlo individualmente
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())    // Guardamos el rol para autorización rápida
                .claim("type", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public GeneratedToken generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds());

        // El Refresh Token es de larga duración y lleva la mínima información posible
        String tokenString = Jwts.builder()
                .subject(user.getEmail())
                .issuer("jwt-ejemplo")
                .id(UUID.randomUUID().toString())
                .claim("uid", user.getId())
                .claim("type", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();

        return new GeneratedToken(tokenString, expiry);
    }

    // =================================================================================
    // VALIDACIÓN
    // =================================================================================

    /**
     * Valida un Access Token entrante.
     * <p>
     * Realiza 3 comprobaciones críticas:
     * 1. <strong>Firma:</strong> ¿Fue generado por nosotros? (Matemáticas)
     * 2. <strong>Expiración:</strong> ¿Sigue vigente? (Tiempo)
     * 3. <strong>Blacklist:</strong> ¿Fue revocado explícitamente? (Negocio)
     * </p>
     */
    @Override
    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = getAllClaims(token);

            String type = claims.get("type", String.class);
            if (!"ACCESS".equals(type)) {
                log.warn("Token rejected: Expected type ACCESS but found {}", type);
                return false;
            }

            String jti = claims.getId();
            if (revokedTokenRepository.isRevoked(jti)) {
                log.warn("Access Token rejected: JTI {} is revoked", jti);
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid Access JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida un Refresh Token entrante.
     */
    @Override
    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = getAllClaims(token);

            String type = claims.get("type", String.class);
            if (!"REFRESH".equals(type)) {
                log.warn("Refresh Token rejected: Expected type REFRESH but found {}", type);
                return false;
            }

            String jti = claims.getId();
            if (revokedTokenRepository.isRevoked(jti)) {
                log.warn("Refresh Token rejected: JTI {} is revoked", jti);
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid Refresh JWT token: {}", e.getMessage());
            return false;
        }
    }

    // =================================================================================
    // ADAPTACIÓN A SPRING SECURITY
    // =================================================================================

    /**
     * Convierte un JWT crudo en un objeto de Autenticación oficial de Spring.
     * Esto permite usar anotaciones como @PreAuthorize("hasRole('ADMIN')") en los controladores.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getAllClaims(token);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);     // Extraemos el claim personalizado

        // Convertimos el rol (String) en una Authority de Spring
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        // Creamos un User interno de Spring (UserDetails) mínimo
        var principal = new org.springframework.security.core.userdetails.User(username, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // =================================================================================
    // EXTRACCIÓN DE DATOS (HELPERS)
    // =================================================================================

    /**
     * Método genérico para extraer cualquier dato del token de forma segura.
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    @Override
    public String getJtiFromToken(String token) {
        return getClaim(token, Claims::getId);
    }

    @Override
    public Instant getExpirationFromToken(String token) {
        return getClaim(token, Claims::getExpiration).toInstant();
    }
}