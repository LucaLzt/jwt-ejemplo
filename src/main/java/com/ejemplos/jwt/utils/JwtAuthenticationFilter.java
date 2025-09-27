package com.ejemplos.jwt.utils;

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

    /**
     * Ejecuta el filtro en cada request entrante.
     * - Omite las rutas que comienzan con "/api/auth" (login, register, refresh, logout).
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
            if (isAuthRequest(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = getTokenFromRequest(request);

            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtUtil.getUsernameFromToken(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = getUserDetails(username);

                if (jwtUtil.isAccessTokenValid(token, userDetails)) {
                    setAuthentication(request, userDetails);
                }
            }

            filterChain.doFilter(request, response);
        } catch (MalformedJwtException e) {
            handleErrorToken(response, "Malformed JWT: " + e.getMessage());
        } catch (JwtException e) {
            handleErrorToken(response, "JWT Exception: " + e.getMessage());
        } catch (IOException e) {
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
     * Verifica si la solicitud es una solicitud de autenticación.
     *
     * @param request La solicitud HTTP entrante.
     * @return true si la solicitud es para autenticación, false en caso contrario.
     */
    private boolean isAuthRequest(HttpServletRequest request) {
        return request.getServletPath().contains("/api/auth");
    }

    /**
     * Obtiene los detalles del usuario a partir del nombre de usuario.
     *
     * @param username El nombre de usuario del usuario.
     * @return Los detalles del usuario.
     */
    private UserDetails getUserDetails(String username) {
        return userDetailsService.loadUserByUsername(username);
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
