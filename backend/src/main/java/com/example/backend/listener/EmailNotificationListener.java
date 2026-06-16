package com.example.backend.listener;

import com.example.backend.event.AccountLockedEvent;
import com.example.backend.event.NewLoginEvent;
import com.example.backend.event.TwoFactorChangedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationListener.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotificationListener(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Async
    @EventListener
    public void onNewLogin(NewLoginEvent event) {
        String subject = "Novo acesso à sua conta detectado";
        String body = """
                <p>Olá,</p>
                <p>Detectamos um novo acesso à sua conta em <strong>%s UTC</strong> a partir do IP <strong>%s</strong>.</p>
                <p>Se não foi você, recomendamos que altere sua senha imediatamente e encerre todas as sessões ativas.</p>
                <br><p>Equipe de Segurança</p>
                """.formatted(event.loginAt(), event.ip());

        send(event.email(), subject, body);
    }

    @Async
    @EventListener
    public void onTwoFactorChanged(TwoFactorChangedEvent event) {
        String action = event.enabled() ? "ativada" : "desativada";
        String subject = "Autenticação de dois fatores " + action;
        String body = """
                <p>Olá,</p>
                <p>A autenticação de dois fatores (2FA) da sua conta foi <strong>%s</strong> em <strong>%s UTC</strong> a partir do IP <strong>%s</strong>.</p>
                <p>Se não foi você, acesse sua conta imediatamente e reverta a alteração.</p>
                <br><p>Equipe de Segurança</p>
                """.formatted(action, event.changedAt(), event.ip());

        send(event.email(), subject, body);
    }

    @Async
    @EventListener
    public void onAccountLocked(AccountLockedEvent event) {
        String subject = "Sua conta foi temporariamente bloqueada";
        String body = """
                <p>Olá,</p>
                <p>Detectamos múltiplas tentativas de login inválidas na sua conta.</p>
                <p>Por segurança, o acesso foi bloqueado até <strong>%s UTC</strong>.</p>
                <p>Se não foi você, recomendamos alterar sua senha assim que o bloqueio expirar.</p>
                <br><p>Equipe de Segurança</p>
                """.formatted(event.lockedUntil());

        send(event.email(), subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MailException | MessagingException exception) {
            LOGGER.warn("Falha ao enviar e-mail de segurança para {}: {}", to, exception.getMessage());
        }
    }
}