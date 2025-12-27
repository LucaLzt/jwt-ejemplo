package com.ejemplos.jwt.infrastructure.web;

import com.ejemplos.jwt.application.ports.in.*;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.domain.exception.personalized.InvalidTokenException;
import com.ejemplos.jwt.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        LoginCommand command = new LoginCommand(
                request.email(),
                request.password()
        );
        LoginResult result = loginUseCase.login(command);
        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(),
                result.refreshToken()
        ));
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
        RefreshTokenResult refreshTokenResult = refreshTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(new RefreshResponse(
                refreshTokenResult.accessToken(),
                refreshTokenResult.refreshToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody LogoutRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid Authorization header");
        }

        String token = authHeader.substring(7);

        String jti = jwtTokenProviderPort.getJtiFromToken(token);
        Instant expiration = jwtTokenProviderPort.getExpirationFromToken(token);
        String email = jwtTokenProviderPort.getUsernameFromToken(token);

        LogoutCommand command = new LogoutCommand(
                jti,
                email,
                expiration,
                request.refreshToken()
        );

        logoutUseCase.logout(command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recovery")
    public ResponseEntity<Void> requestRecovery(@Valid @RequestBody RequestRecoveryRequest request) {
        requestRecoveryUseCase.requestRecovery(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(new ResetPasswordCommand(
                request.token(),
                request.newPassword()
        ));
        return ResponseEntity.ok().build();
    }
}
