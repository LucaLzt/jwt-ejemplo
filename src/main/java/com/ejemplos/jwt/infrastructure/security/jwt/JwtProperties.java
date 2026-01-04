package com.ejemplos.jwt.infrastructure.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración mapeada al archivo application.yml.
 * <p>
 * Lee las propiedades con el prefijo "jwt" (secret-key, tiempos de expiración)
 * para mantener las credenciales fuera del código compilado.
 * </p>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey;
    private long accessTokenExpirationSeconds;
    private long refreshTokenExpirationSeconds;
}
