package com.ejemplos.jwt.utils;

import com.ejemplos.jwt.repositories.RevokedTokenRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT que verifica la validez del token en cada solicitud.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Determina si el filtro debe aplicarse a la solicitud actual.
     * Omite las rutas que comienzan con "/api/auth/".
     *
     * @param request
     * @return true si la solicitud no debe ser filtrada, false en caso contrario.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/auth/");
    }

    /**
     * Ejecuta el filtro en cada request entrante.
     * - Extrae el bearer token del header Authorization.
     * - Valida el token como accessToken y, si es válido, setea la autenticación en el SecurityContext.
     *
     * @param request  La request HTTP entrante.
     * @param response La response HTTP.
     * @param filterChain La cadena de filtros que debe continuar.
     * @throws ServletException si ocurre un error en el filtro.
     * @throws IOException si ocurre un error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            String token = getTokenFromRequest(request);
            if (token != null) {
                String username = jwtUtil.getUsernameFromToken(token);
                String jti = jwtUtil.getJti(token);

                if (revokedTokenRepository.existsByJti(jti)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Unauthorized: token revoked");
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isAccessTokenValid(token, userDetails)) {
                    setAuthentication(request, userDetails);
                }
            }

            filterChain.doFilter(request, response);
        } catch (MalformedJwtException e) {
            System.out.println("Malformed JWT: " + e.getMessage());
            handleErrorToken(response, "Malformed JWT: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("JWT Exception: " + e.getMessage());
            handleErrorToken(response, "JWT Exception: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Extrae el token JWT desde el header Authorization.
     *
     * @param request La request HTTP entrante.
     * @return El token JWT o null si no existe.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            return header.replace("Bearer ", "");
        }

        return null;
    }

    /**
     * Establece la autenticación en el contexto de seguridad de Spring.
     *
     * @param request La request HTTP.
     * @param userDetails Los datos del usuario autenticado.
     */
    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * Maneja un error relacionado con el token JWT y responde con un mensaje de error.
     *
     * @param response La respuesta HTTP saliente.
     * @param error    El mensaje de error a incluir en la respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleErrorToken(HttpServletResponse response, String error) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + error + "\"}");
    }

}
