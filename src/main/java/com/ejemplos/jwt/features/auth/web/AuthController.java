package com.ejemplos.jwt.features.auth.web;

import com.ejemplos.jwt.features.auth.application.dto.AuthDTO;
import com.ejemplos.jwt.features.auth.application.dto.LoginDTO;
import com.ejemplos.jwt.features.auth.application.dto.RefreshDTO;
import com.ejemplos.jwt.features.auth.application.dto.RegisterDTO;
import com.ejemplos.jwt.features.auth.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        try {
            AuthDTO tokens = authService.login(dto);
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
            AuthDTO tokens = authService.register(dto);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Error registering user\"}");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshDTO dto) {
        try {
            AuthDTO tokens = authService.refresh(dto.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Invalid or expired refresh token\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearer) {
        authService.logoutByAccessToken(bearer);
        return ResponseEntity.noContent().build();
    }

}
