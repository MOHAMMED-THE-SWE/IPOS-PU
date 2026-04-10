package interfaces;

import jakarta.mail.MessagingException;

/**
 * IEmailService — low-level email sender used by EmailDispatcher.
 * Implementation: SmtpEmailService (Gmail SMTP).
 */
public interface IEmailService {

    /**
     * Sends an email via SMTP.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body
     * @return true if email was sent successfully
     * @throws MessagingException if SMTP communication fails
     */
    boolean sendEmail(String to, String subject, String body) throws MessagingException;
}
