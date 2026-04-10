package interfaces;

import java.util.List;

/**
 * IEmailGateway — provided by IPOS-PU to other subsystems (SA, CA).
 * Used to queue system emails. Implementations must validate inputs
 * and throw IllegalArgumentException on invalid email or null subject.
 */
public interface IEmailGateway {

    /**
     * Queues a system email for sending.
     *
     * @param to      recipient email address (must be valid format)
     * @param subject email subject (must not be null)
     * @param body    email body text
     * @return true if the email was successfully queued
     * @throws IllegalArgumentException if to is invalid or subject is null
     */
    boolean sendSystemEmail(String to, String subject, String body);

    /**
     * Queues the same system email to multiple recipients.
     * Each address is validated individually.
     *
     * @param recipients list of recipient email addresses (must not be null or empty)
     * @param subject    email subject (must not be null)
     * @param body       email body text
     * @return true if all emails were successfully queued
     * @throws IllegalArgumentException if recipients is null/empty, any address is invalid, or subject is null
     */
    boolean sendBulkSystemEmail(List<String> recipients, String subject, String body);
}
