package com.ejemplos.jwt.services.impl;

import com.ejemplos.jwt.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Implementación basada en JavaMailSender (SMTP).
 * Centraliza el formato de los mensajes y maneja excepciones del proveedor.
 *
 * @param mailSender Bean configurado de Spring para SMTP.
 * @return void
 * @exception org.springframework.mail.MailException si falla el envío.
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendBasicEmail(String to, String subject, String content) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        mailSender.send(msg);
    }

    @Override
    public void sendPasswordReset(String to, String link) {
        String subject = "Password Reset Request";
        String body = "To reset your password, click the following link:\n" + link +
                      "\n\nIf you did not request a password reset, please ignore this email." +
                      "\nThis link will expire in 15 minutes.";
        sendBasicEmail(to, subject, body);
    }

}
