package com.ejemplos.jwt.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/public")
@Tag(name = "Endpoints Públicos", description = "Endpoints de prueba accesibles sin autenticación para verificar la configuración de seguridad")
public class PublicController {

    @GetMapping
    @Operation(summary = "Prueba de acceso público GET", description = "Verifica que las peticiones GET públicas están permitidas")
    public String publicGetEndpoint() {
        return "This message is returned because you accessed a public GET endpoint.";
    }

    @PostMapping
    @Operation(summary = "Prueba de acceso público POST", description = "Verifica que las peticiones POST públicas están permitidas")
    public String publicPostEndpoint() {
        return "This message is returned because you accessed a public POST endpoint.";
    }

    @PutMapping
    @Operation(summary = "Prueba de acceso público PUT", description = "Verifica que las peticiones PUT públicas están permitidas")
    public String publicPutEndpoint() {
        return "This message is returned because you accessed a public PUT endpoint.";
    }

    @DeleteMapping
    @Operation(summary = "Prueba de acceso público DELETE", description = "Verifica que las peticiones DELETE públicas están permitidas")
    public String publicDeleteEndpoint() {
        return "This message is returned because you accessed a public DELETE endpoint.";
    }
}
