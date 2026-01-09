package org.mehlib.marked.service;

/**
 * Service interface for sending emails.
 */
public interface MailService {

    /**
     * Send an attendance verification token to a student's email.
     *
     * @param toEmail     the recipient's email address
     * @param studentName the student's name (for personalization)
     * @param token       the verification token
     * @param sessionInfo a description of the session (e.g., "Algorithms - Mon, Jan 15 09:00-10:30")
     * @param expiryMinutes how many minutes until the token expires
     */
    void sendAttendanceToken(
        String toEmail,
        String studentName,
        String token,
        String sessionInfo,
        int expiryMinutes
    );

    /**
     * Send a simple text email.
     *
     * @param to      the recipient's email address
     * @param subject the email subject
     * @param body    the email body (plain text)
     */
    void sendSimpleEmail(String to, String subject, String body);

    /**
     * Check if the mail service is properly configured and can send emails.
     *
     * @return true if email sending is enabled and configured
     */
    boolean isEmailEnabled();
}
