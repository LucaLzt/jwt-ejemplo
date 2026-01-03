package com.ejemplos.jwt.infrastructure.web;

import com.ejemplos.jwt.application.ports.in.*;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.infrastructure.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para login, registro, renovación de tokens y recuperación de contraseñas")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final JwtTokenProviderPort jwtTokenProviderPort;
    private final LogoutUseCase logoutUseCase;
    private final RequestRecoveryUseCase requestRecoveryUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ChangeRoleUseCase changeRoleUseCase;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica las credenciales del usuario y emite un par de tokens (Access + Refresh).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
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
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta de usuario con rol CLIENT por defecto.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o contraseña débil", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "409", description = "El email ya está registrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
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
    @Operation(
            summary = "Refrescar Token",
            description = "Obtiene un nuevo Access Token usando un Refresh Token válido. El Refresh Token anterior se invalida (Rotación).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token renovado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Refresh token con formato inválido", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "403", description = "Refresh token expirado, revocado o no encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshTokenResult refreshTokenResult = refreshTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(new RefreshResponse(
                refreshTokenResult.accessToken(),
                refreshTokenResult.refreshToken())
        );
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el Refresh Token y la sesión actual. Requiere Header Authorization Bearer.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Logout exitoso (Sin contenido)"),
                    @ApiResponse(responseCode = "401", description = "Token no proporcionado o inválido", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String accessToken = (String) authentication.getCredentials();

        String jti = jwtTokenProviderPort.getJtiFromToken(accessToken);
        Instant expiration = jwtTokenProviderPort.getExpirationFromToken(accessToken);
        String email = authentication.getName();

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
    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = "Envía un correo con un token de recuperación si el email existe.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud procesada (siempre devuelve 200 por seguridad para no revelar usuarios)"),
                    @ApiResponse(responseCode = "400", description = "Email con formato inválido", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<Void> requestRecovery(@Valid @RequestBody RequestRecoveryRequest request) {
        requestRecoveryUseCase.requestRecovery(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Restablecer contraseña",
            description = "Cambia la contraseña usando un token de recuperación válido.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Token inválido, expirado o password no cumple requisitos", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(new ResetPasswordCommand(
                request.token(),
                request.newPassword()
        ));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-role")
    @Operation(
        summary = "Alternar mi propio rol (Testing)",
        description = "Cambia tu rol actual (ADMIN <-> CLIENT). REQUIERE RELOGUEARSE para que el nuevo token tenga el rol actualizdo.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Rol cambiado exitosamente. Por favor, vuelva a iniciar sesión."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado en la base de datos (Token desincronizado).", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    public ResponseEntity<Void> switchMyRole(Authentication authentication, @RequestHeader("Authorization") String authHeader) {
        changeRoleUseCase.changeRole(
                new ChangeRoleCommand(
                    authentication.getName(),
                    authHeader.replace("Bearer ", "")
                )
        );
        return ResponseEntity.ok().build();
    }
}
