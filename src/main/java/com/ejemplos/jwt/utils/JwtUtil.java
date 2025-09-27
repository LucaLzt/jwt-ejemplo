package com.ejemplos.jwt.utils;

import com.ejemplos.jwt.models.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase utilitaria para la generación y validación de tokens JWT.
 */
@Service
public class JwtUtil {

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.accessExpiration}")
    private Long ACCESS_EXPIRATION;

    @Value("${jwt.refreshExpiration}")
    private Long REFRESH_EXPIRATION;

    /**
     * Genera un accessToken para un usuario.
     *
     * @param user El usuario autenticado.
     * @return Un accessToken JWT firmado con expiración corta.
     */
    public String generateAccessToken(User user) {
        return buildToken(user, ACCESS_EXPIRATION, "ACCESS");
    }

    /**
     * Genera un refreshToken para un usuario.
     *
     * @param user El usuario autenticado.
     * @return Un refreshToken JWT firmado con expiración larga.
     */
    public String generateRefreshToken(User user) {
        return buildToken(user, REFRESH_EXPIRATION, "REFRESH");
    }

    /**
     * Construye un token JWT con claims, expiración y tipo.
     *
     * @param user El usuario.
     * @param expirationMillis Tiempo de expiración en milisegundos.
     * @param tokenType El tipo de token ("ACCESS" o "REFRESH").
     * @return El token JWT firmado.
     */
    public String buildToken(User user, long expirationMillis, String tokenType) {
        // Fecha de emisión y expiración
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + expirationMillis);

        // Claims adicionales
        Map<String, Object> claims = generateClaims(user);
        claims.put("token_type", tokenType);

        // Clave de firma
        SecretKey key = getKey();
        MacAlgorithm signatureAlgorithm = Jwts.SIG.HS256;

        return Jwts.builder()
                .header().type("JWT").and()
                .subject(user.getEmail())
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    /**
     * Valida que un accessToken sea correcto.
     *
     * @param token El token JWT a validar.
     * @param userDetails Los detalles del usuario.
     * @return true si es válido, false en caso contrario.
     */
    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return "ACCESS".equalsIgnoreCase(getTokenType(token))
                && userDetails.getUsername().equals(getUsernameFromToken(token))
                && !isTokenExpired(token);
    }

    /**
     * Valida que un refreshToken sea correcto.
     *
     * @param token El refreshToken.
     * @throws io.jsonwebtoken.JwtException si el token no es de tipo REFRESH o está expirado.
     */
    public void validateRefreshToken(String token) {
        if (!"REFRESH".equalsIgnoreCase(getTokenType(token))) {
            throw new JwtException("Invalid token_type for refresh");
        }
        if (isTokenExpired(token)) {
            throw new JwtException("Refresh token expired");
        }
    }

    /**
     * Obtiene el username (subject) desde un token JWT.
     *
     * @param token El token JWT.
     * @return El email del usuario.
     */
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Obtiene el tipo de token (ACCESS o REFRESH).
     *
     * @param token El token JWT.
     * @return El tipo de token.
     */
    private String getTokenType(String token) {
        return getClaim(token, claims -> claims.get("token_type", String.class));
    }

    /**
     * Obtiene la fecha de expiración de un token JWT.
     *
     * @param token El token JWT.
     * @return La fecha de expiración.
     */
    private Date getExpirationFromToken(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Construye la clave secreta para firmar/verificar los tokens.
     *
     * @return La clave secreta derivada de la propiedad jwt.secret.
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    /**
     * Genera los claims básicos a partir de un usuario.
     *
     * @param user El usuario.
     * @return Un mapa con los claims del token.
     */
    private Map<String, Object> generateClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getId());
        claims.put("role", user.getRole().name());
        return claims;
    }

    /**
     * Obtiene un claim específico desde un token.
     *
     * @param token El token JWT.
     * @param claimsResolver Función que extrae el claim.
     * @param <T> Tipo de dato esperado.
     * @return El valor del claim.
     */
    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        Claims payload = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(payload);
    }

    /**
     * Verifica si un token ya está expirado.
     *
     * @param token El token JWT.
     * @return true si expiró, false en caso contrario.
     */
    private Boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }

}
