package com.ejemplos.jwt.infrastructure.web;

import com.ejemplos.jwt.application.ports.in.*;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final JwtTokenProviderPort jwtTokenProviderPort;
    private final LogoutUseCase logoutUseCase;
    private final RequestRecoveryUseCase requestRecoveryUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginCommand command = new LoginCommand(
                    request.email(),
                    request.password()
            );
            LoginResult result = loginUseCase.login(command);
            return ResponseEntity.ok(new LoginResponse(
                    result.accessToken(),
                    result.refreshToken()
            ));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        RegisterCommand command = new RegisterCommand(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password()
        );
        registerUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            String newAccessToken = refreshTokenUseCase.refresh(request.refreshToken());
            return ResponseEntity.ok(new RefreshResponse(newAccessToken));
        } catch (Exception e) {
            throw new IllegalArgumentException("The refresh token is invalid or expired");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);

        String jti = jwtTokenProviderPort.getJtiFromToken(token);
        Instant expiration = jwtTokenProviderPort.getExpirationFromToken(token);
        String email = jwtTokenProviderPort.getUsernameFromToken(token);

        LogoutCommand command = new LogoutCommand(
                jti,
                email,
                expiration
        );

        logoutUseCase.logout(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recovery")
    public ResponseEntity<Void> requestRecovery(@RequestBody RequestRecoveryRequest request) {
        requestRecoveryUseCase.requestRecovery(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        ResetPasswordCommand command = new ResetPasswordCommand(
                request.token(),
                request.newPassword()
        );
        resetPasswordUseCase.resetPassword(command);
        return ResponseEntity.ok().build();
    }
}
