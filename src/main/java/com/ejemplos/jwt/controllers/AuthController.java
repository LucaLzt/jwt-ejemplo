package com.ejemplos.jwt.controllers;

import com.ejemplos.jwt.models.dtos.LoginDTO;
import com.ejemplos.jwt.models.dtos.RegisterDTO;
import com.ejemplos.jwt.models.dtos.RefreshTokenRequestDTO;
import com.ejemplos.jwt.services.AuthService;
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
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.login(dto));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Invalid credentials\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.register(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Error registering user\"}");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.refreshToken(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequestDTO dto) {
        try {
            service.logout(dto.getRefreshToken());
            return ResponseEntity.status(HttpStatus.OK)
                    .body("{\"message\": \"Logged out successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

}
