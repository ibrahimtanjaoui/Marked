package org.mehlib.marked.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Implementation of MailService using Spring's JavaMailSender.
 * Configured to work with Gmail SMTP.
 */
@Service
public class MailManager implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailManager.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@marked.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Marked Attendance System}")
    private String fromName;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public MailManager(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendAttendanceToken(
        String toEmail,
        String studentName,
        String token,
        String sessionInfo,
        int expiryMinutes
    ) {
        String subject = "Your Attendance Verification Code - " + token;

        String body = String.format("""
            Hello %s,

            You have requested to mark your attendance for:
            %s

            Your verification code is: %s

            This code will expire in %d minutes.

            Please enter this code on the attendance page to confirm your presence.

            IMPORTANT: Do not share this code with anyone. Each code is unique to you
            and can only be used once.

            If you did not request this code, please ignore this email.

            ---
            Marked Attendance System
            This is an automated message, please do not reply.
            """,
            studentName,
            sessionInfo,
            token,
            expiryMinutes
        );

        sendSimpleEmail(toEmail, subject, body);
        log.info("Sent attendance token email to {} for session: {}", toEmail, sessionInfo);
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        if (!isEmailEnabled()) {
            log.warn("Email is not configured. Would have sent email to: {} with subject: {}", to, subject);
            log.debug("Email body: {}", body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(String.format("%s <%s>", fromName, fromEmail));
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Successfully sent email to: {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to: {} - Error: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEmailEnabled() {
        return mailUsername != null && !mailUsername.isBlank();
    }
}
