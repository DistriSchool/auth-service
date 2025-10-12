package edu.unifor.br.distrischool.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    public void sendVerificationEmail(String toEmail, String token) {
        if (!mailEnabled || fromEmail == null || fromEmail.isEmpty() || fromEmail.equals("dummy@email.com")) {
            log.warn("Email desabilitado. Link de verificação: {}/verify-email?token={}", frontendUrl, token);
            return;
        }

        try {
            String verificationLink = frontendUrl + "/verify-email?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verificação de Email - Sistema Escolar");
            message.setText(
                    "Olá,\n\n" +
                            "Obrigado por se registrar em nosso sistema escolar!\n\n" +
                            "Para ativar sua conta, clique no link abaixo:\n" +
                            verificationLink + "\n\n" +
                            "Este link expira em 24 horas.\n\n" +
                            "Se você não criou esta conta, ignore este email.\n\n" +
                            "Atenciosamente,\n" +
                            "Equipe Sistema Escolar"
            );

            mailSender.send(message);
            log.info("Email de verificação enviado para: {}", toEmail);
        } catch (Exception e) {
            log.error("Erro ao enviar email de verificação: {}", e.getMessage(), e);
            log.warn("Link de verificação não enviado. Use: {}/verify-email?token={}", frontendUrl, token);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        if (!mailEnabled || fromEmail == null || fromEmail.isEmpty() || fromEmail.equals("dummy@email.com")) {
            log.warn("Email desabilitado. Link de reset: {}/reset-password?token={}", frontendUrl, token);
            return;
        }

        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Recuperação de Senha - Sistema Escolar");
            message.setText(
                    "Olá,\n\n" +
                            "Recebemos uma solicitação para redefinir sua senha.\n\n" +
                            "Para criar uma nova senha, clique no link abaixo:\n" +
                            resetLink + "\n\n" +
                            "Este link expira em 1 hora.\n\n" +
                            "Se você não solicitou esta alteração, ignore este email.\n\n" +
                            "Atenciosamente,\n" +
                            "Equipe Sistema Escolar"
            );

            mailSender.send(message);
            log.info("Email de recuperação de senha enviado para: {}", toEmail);
        } catch (Exception e) {
            log.error("Erro ao enviar email de recuperação: {}", e.getMessage(), e);
            log.warn("Link de reset não enviado. Use: {}/reset-password?token={}", frontendUrl, token);
        }
    }
}
