package com.ejemplos.jwt.services.impl;

import com.ejemplos.jwt.models.dtos.AuthDTO;
import com.ejemplos.jwt.models.dtos.LoginDTO;
import com.ejemplos.jwt.models.dtos.RegisterDTO;
import com.ejemplos.jwt.models.dtos.RefreshTokenRequestDTO;
import com.ejemplos.jwt.models.entities.RefreshToken;
import com.ejemplos.jwt.models.entities.User;
import com.ejemplos.jwt.repositories.UserRepository;
import com.ejemplos.jwt.services.AuthService;
import com.ejemplos.jwt.services.RefreshTokenService;
import com.ejemplos.jwt.utils.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/*
 * Implementación del servicio de autenticación.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    /**
     * Realiza la autenticación de un usuario.
     *
     * @param login Los datos de inicio de sesión del usuario.
     * @return Un objeto AuthDTO que contiene el access token y refresh token.
     * @throws Exception si ocurre un error durante el proceso de autenticación.
     */
    @Override
    public AuthDTO login(LoginDTO login) throws Exception {
        try {
            authenticate(login.getEmail(), login.getPassword());

            User user = userRepository.findByEmail(login.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String accessToken = jwtUtil.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return new AuthDTO(accessToken, refreshToken.getToken());
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            System.out.println(e.getMessage());
            throw new BadCredentialsException("Incorrect username or password");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param register Los datos de registro del nuevo usuario.
     * @return Un objeto AuthDTO que contiene el access token y refresh token.
     * @throws Exception Si ocurre un error durante el proceso de registro.
     */
    @Override
    @Transactional
    public AuthDTO register(RegisterDTO register) throws Exception {
        try {
            User user = createUserFromRegistration(register);
            user = userRepository.save(user);

            String accessToken = jwtUtil.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return new AuthDTO(accessToken, refreshToken.getToken());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Renueva el access token usando un refresh token válido.
     *
     * @param refreshTokenRequest La solicitud que contiene el refresh token.
     * @return Un objeto AuthDTO con un nuevo access token y el mismo refresh token.
     * @throws Exception si el refresh token es inválido o ha expirado.
     */
    @Override
    public AuthDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest) throws Exception {
        try {
            String requestRefreshToken = refreshTokenRequest.getRefreshToken();

            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            refreshToken = refreshTokenService.verifyExpiration(refreshToken);

            User user = refreshToken.getUser();
            String newAccessToken = jwtUtil.generateToken(user);

            return new AuthDTO(newAccessToken, requestRefreshToken);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Could not refresh token: " + e.getMessage());
        }
    }

    /**
     * Cierra la sesión del usuario revocando el refresh token.
     *
     * @param refreshTokenValue El refresh token a revocar.
     * @throws Exception si ocurre un error durante el logout.
     */
    @Override
    @Transactional
    public void logout(String refreshTokenValue) throws Exception {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            refreshTokenService.revokeToken(refreshToken);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Could not logout: " + e.getMessage());
        }
    }

    /**
     * Autentica al usuario utilizando el gestor de autenticación.
     *
     * @param username El nombre de usuario del usuario.
     * @param password La contraseña del usuario.
     */
    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * Crea un nuevo objeto de usuario a partir de los datos de registro.
     *
     * @param register Los datos de registro del nuevo usuario.
     * @return El usuario creado.
     */
    private User createUserFromRegistration(RegisterDTO register) {
        User user = new User();
        user.setName(register.getName());
        user.setLastName(register.getLastName());
        user.setEmail(register.getEmail());
        user.setPassword(passwordEncoder.encode(register.getPassword()));
        user.setRole(register.getRole());

        return user;
    }

}
