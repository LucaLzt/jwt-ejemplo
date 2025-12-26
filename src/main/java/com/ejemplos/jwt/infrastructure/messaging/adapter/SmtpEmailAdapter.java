package com.ejemplos.jwt.infrastructure.messaging.adapter;

import com.ejemplos.jwt.application.ports.out.EmailNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpEmailAdapter implements EmailNotificationPort {

    private final JavaMailSender mailSender;

    @Override
    public void sendRecoveryEmail(String to, String link) {
        String subject = "Password Reset Request";
        String body = "To reset your password, click the following link:\n" + link +
                      "\n\nIf you did not request a password reset, please ignore this email." +
                      "\nThis link will expire in 15 minutes.";

        sendBasicEmail(to, subject, body);
    }

    private void sendBasicEmail(String to, String subject, String content) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        mailSender.send(msg);
    }
}
