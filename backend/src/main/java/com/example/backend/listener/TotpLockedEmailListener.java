package com.example.backend.listener;

import com.example.backend.event.TotpLockedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class TotpLockedEmailListener {

    private final JavaMailSender mailSender;
    private final String from;

    public TotpLockedEmailListener(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @EventListener
    public void onTotpLocked(TotpLockedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(event.email());
        message.setSubject("Alerta de segurança: múltiplas tentativas inválidas no 2FA");
        message.setText(
                "Olá,\n\n" +
                "Detectamos múltiplas tentativas inválidas de código TOTP na sua conta.\n" +
                "Por segurança, o acesso via 2FA foi temporariamente bloqueado até: " + event.lockedUntil() + ".\n\n" +
                "Se não foi você, revise imediatamente a segurança da sua conta.\n\n" +
                "Equipe de Segurança"
        );

        mailSender.send(message);
    }
}