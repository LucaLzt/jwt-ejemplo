package com.ejemplos.jwt.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
@Tag(name = "Usuarios (Protegido)", description = "Endpoints de prueba para validar autorización basada en Roles (RBAC)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Endpoint solo para Administradores",
            description = "Requiere que el usuario tenga el rol 'ADMIN' en su JWT.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Acceso permitido"),
                    @ApiResponse(responseCode = "403", description = "Forbidden: Tienes token válido pero NO eres ADMIN", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public String adminEndpoint() {
        return "If you see this, then you are an administrator.";
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(
            summary = "Endpoint solo para Clientes",
            description = "Requiere que el usuario tenga el rol 'CLIENT' en su JWT.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Acceso permitido"),
                    @ApiResponse(responseCode = "403", description = "Forbidden: Tienes token válido pero NO eres CLIENT", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public String clientEndpoint() {
        return "If you see this, then you are a client";
    }

    @GetMapping("/common")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(
            summary = "Endpoint común",
            description = "Accesible para cualquier usuario autenticado con rol ADMIN o CLIENT."
    )
    public String commonEndpoint() {
        return "If you see this, then you are an administrator or a client.";
    }
}
