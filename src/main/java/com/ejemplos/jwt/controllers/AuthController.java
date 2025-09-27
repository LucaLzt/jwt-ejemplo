package com.ejemplos.jwt.controllers;

import com.ejemplos.jwt.models.dtos.AuthDTO;
import com.ejemplos.jwt.models.dtos.LoginDTO;
import com.ejemplos.jwt.models.dtos.RegisterDTO;
import com.ejemplos.jwt.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        try {
            AuthDTO tokens = service.login(dto);
            return ResponseEntity.ok(tokens);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Invalid credentials\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        try {
            AuthDTO tokens = service.register(dto);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Error registering user\"}");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest body) {
        try {
            AuthDTO tokens = service.refresh(body.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Invalid or expired refresh token\"}");
        }
    }

    @PostMapping("/refresh-cookie")
    public ResponseEntity<?> refreshFromCookie(HttpServletRequest request) {
        try {
            String rt = extractCookie(request, "refreshToken");
            AuthDTO tokens = service.refresh(rt);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Invalid or expired refresh token\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String email) {
        service.logout(email);
        return ResponseEntity.noContent().build();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    @Data
    public static class RefreshRequest {
        private String refreshToken;
    }

}
