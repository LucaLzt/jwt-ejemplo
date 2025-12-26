package com.ejemplos.jwt.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
public class UserController {
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "If you see this, then you are an administrator.";
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public String clientEndpoint() {
        return "If you see this, then you are a client";
    }

    @GetMapping("/common")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public String commonEndpoint() {
        return "If you see this, then you are an administrator or a client.";
    }
}
