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

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProviderAdapter implements JwtTokenProviderPort {

    private final JwtProperties jwtProperties;
    private final RevokedTokenRepository revokedTokenRepository;

    private SecretKey secretKey;

    @PostConstruct
    private void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.getAccessTokenExpirationSeconds());

        return Jwts.builder()
                .subject(user.getEmail())
                .issuer("jwt-ejemplo")
                .id(UUID.randomUUID().toString())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
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

    @Override
    public Authentication getAuthentication(String token) {
        Claims claims = getAllClaims(token);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        var principal = new org.springframework.security.core.userdetails.User(username, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

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