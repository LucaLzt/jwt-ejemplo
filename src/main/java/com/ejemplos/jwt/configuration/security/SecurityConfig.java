package com.ejemplos.jwt.configuration.security;

import com.ejemplos.jwt.utils.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *  Clase de configuración de seguridad para la aplicación.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    /**
     * Configura el filtro de seguridad y la cadena de filtros de seguridad HTTP.
     *
     * @param http La configuración de seguridad HTTP.
     * @return La cadena de filtros de seguridad HTTP configurada.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF (Cross-Site Request Forgery)
                .csrf(AbstractHttpConfigurer::disable)
                // Autoriza las peticiones HTTP mediante el objeto authorizationManangerRequestMatcherRegistry
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                        .requestMatchers("/api/auth/**").permitAll()    // Permite el acceso a todas las URL que comiencen con '/api/auth/'
                        .requestMatchers("/api/public/**").permitAll()  // Permite el acceso a todas las URL que comiencen con '/api/public/'
                        .anyRequest().authenticated()                   // Requiere autenticación para cualquier otra solicitud
                )
                // Configura la gestión de sesiones para que sea sin estado (stateless)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Configura el proveedor de autenticación
                .authenticationProvider(
                        authenticationProvider
                )
                // Añade el filtro de autenticación JWT antes del filtro de autenticación de usuario y contraseña
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

}
