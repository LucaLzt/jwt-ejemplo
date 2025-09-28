package com.ejemplos.jwt.services.impl;

import com.ejemplos.jwt.models.dtos.AuthDTO;
import com.ejemplos.jwt.models.dtos.LoginDTO;
import com.ejemplos.jwt.models.dtos.RegisterDTO;
import com.ejemplos.jwt.models.entities.RevokedToken;
import com.ejemplos.jwt.models.entities.User;
import com.ejemplos.jwt.repositories.RevokedTokenRepository;
import com.ejemplos.jwt.repositories.UserRepository;
import com.ejemplos.jwt.services.AuthService;
import com.ejemplos.jwt.utils.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

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
    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Realiza la autenticación de un usuario.
     *
     * @param login Los datos de inicio de sesión del usuario.
     * @return Un objeto AuthDTO que contiene el accessToken y el refreshToken.
     * @throws Exception si las credenciales son inválidas o ocurre un error durante la autenticación.
     */
    @Override
    public AuthDTO login(LoginDTO login) throws Exception {
        try {
            authenticate(login.getEmail(), login.getPassword());

            User user = userRepository.findByEmail(login.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            return new AuthDTO(accessToken, refreshToken);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            System.out.println(e.getMessage());
            throw new BadCredentialsException("Incorrect username or password");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param register Los datos de registro del usuario.
     * @return Un objeto AuthDTO que contiene el accessToken y el refreshToken.
     * @throws Exception si ocurre un error durante el registro.
     */
    @Override
    @Transactional
    public AuthDTO register(RegisterDTO register) throws Exception {
        try {
            User user = createUserFromRegistration(register);
            user = userRepository.save(user);

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            return new AuthDTO(accessToken, refreshToken);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Refresca el accessToken de un usuario usando su refreshToken.
     *
     * @param providedRefreshToken El refreshToken válido del usuario.
     * @return Un objeto AuthDTO que contiene un nuevo accessToken y un nuevo refreshToken.
     * @throws UsernameNotFoundException si no se encuentra el usuario asociado al refreshToken.
     * @throws io.jsonwebtoken.JwtException si el refreshToken es inválido o ha expirado.
     */
    @Override
    @Transactional
    public AuthDTO refresh(String providedRefreshToken) {
        jwtUtil.validateRefreshToken(providedRefreshToken);

        User user = userRepository.findByRefreshToken(providedRefreshToken)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        String newAccessToken = jwtUtil.generateAccessToken(user);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
        return new AuthDTO(newAccessToken, newRefreshToken);
    }

    /**
     * Cierra la sesión de un usuario invalidando su refreshToken.
     *
     * @param userEmail El email del usuario que desea cerrar sesión.
     */
    @Override
    @Transactional
    public void logout(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    /**
     * Autentica las credenciales de un usuario contra Spring Security.
     *
     * @param username El email del usuario.
     * @param password La contraseña del usuario.
     * @throws BadCredentialsException si las credenciales son inválidas.
     */
    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * Construye una entidad User a partir de los datos de registro.
     *
     * @param register Los datos del DTO de registro.
     * @return Un objeto User listo para ser persistido en base de datos.
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

    @Transactional
    public void logoutByAccessToken(String bearerToken) {
        String token = bearerToken != null && bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7) : bearerToken;

        String jti = jwtUtil.getJti(token);
        String subject = jwtUtil.getUsernameFromToken(token);
        Instant exp = jwtUtil.getExpirationFromToken(token).toInstant();

        RevokedToken revoked = new RevokedToken();
        revoked.setJti(jti);
        revoked.setSubject(subject);
        revoked.setReason("LOGOUT");
        revoked.setExpiresAt(exp);
        revokedTokenRepository.save(revoked);

        userRepository.findByEmail(subject).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
    }

}
