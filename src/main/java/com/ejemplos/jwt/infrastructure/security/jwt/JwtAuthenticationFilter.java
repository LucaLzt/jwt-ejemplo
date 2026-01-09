package com.ejemplos.jwt.infrastructure.security.jwt;

import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que intercepta CADA petición HTTP para buscar un token JWT.
 * <p>
 * <strong>Funcionamiento:</strong>
 * <ol>
 * <li>Busca el header "Authorization: Bearer ...".</li>
 * <li>Si existe y es válido, extrae el usuario y sus roles.</li>
 * <li>Crea una sesión temporal en el {@link SecurityContextHolder} solo para este request.</li>
 * </ol>
 * Si no hay token, deja pasar la petición (Spring Security decidirá luego si rechazarla o no según la URL).
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProviderAdapter jwtTokenProviderAdapter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTH_HEADER);

        // 1. ¿Viene el token?
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 2. ¿Es válido? (Firma, Expiración, Blacklist)
        if (!jwtTokenProviderAdapter.isAccessTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Autenticar en el contexto
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            var authentication = jwtTokenProviderAdapter.getAuthentication(token);

            if (authentication instanceof AbstractAuthenticationToken authToken) {
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continuar la cadena
        filterChain.doFilter(request, response);
    }
}
