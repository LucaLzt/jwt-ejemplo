package com.ejemplos.jwt.features.recoverypassword.web;

import com.ejemplos.jwt.features.recoverypassword.application.dto.ForgotDTO;
import com.ejemplos.jwt.features.recoverypassword.application.dto.ResetDTO;
import com.ejemplos.jwt.features.recoverypassword.application.service.PasswordRecoveryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recovery")
public class RecoveryPasswordController {

    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotDTO dto, HttpServletRequest request) {
        try {
            String base = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            passwordRecoveryService.requestRecovery(dto.getEmail(), base);
            return ResponseEntity.ok("{\"message\": \"If the email exists, a recovery link has been sent.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error processing password recovery request\"}");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetDTO dto) {
        try {
            passwordRecoveryService.confirmReset(dto.getToken(), dto.getNewPassword(), dto.getRepeatNewPassword());
            return ResponseEntity.ok("{\"message\": \"Password has been reset successfully.\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error resetting password\"}");
        }
    }

}
