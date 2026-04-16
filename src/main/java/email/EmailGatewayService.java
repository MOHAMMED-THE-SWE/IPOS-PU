package email;

import dao.comms.EmailOutboxDAO;
import interfaces.IEmailGateway;
import model.EmailOutbox;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Implements {@link IEmailGateway} for IPOS-PU using the Outbox Pattern.
 *
 * <p>Rather than sending emails synchronously, this service validates the recipient
 * address and subject, then enqueues each message as a {@code PENDING} row in the
 * {@code pu_email_outbox} table. A background {@link EmailDispatcher} processes
 * the outbox every 60 seconds and delivers via SMTP.</p>
 *
 * <p>This approach decouples email delivery from the business workflow, ensuring
 * that SMTP failures do not block checkout or registration operations.</p>
 *
 * @author Team C
 */
public class EmailGatewayService implements IEmailGateway {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$");

    private EmailOutboxDAO emailOutboxDAO;

    /**
     * Creates a new EmailGatewayService.
     *
     * @param emailOutboxDAO DAO used to persist outbound email records
     */
    public EmailGatewayService(EmailOutboxDAO emailOutboxDAO) {
        this.emailOutboxDAO = emailOutboxDAO;
    }

    @Override
    public boolean sendSystemEmail(String to, String subject, String body) {
        if (to == null || !EMAIL_PATTERN.matcher(to).matches()) {
            throw new IllegalArgumentException("Invalid recipient email address: " + to);
        }
        if (subject == null) {
            throw new IllegalArgumentException("Email subject must not be null");
        }

        EmailOutbox email = new EmailOutbox(to, subject, body != null ? body : "");
        emailOutboxDAO.save(email);
        return true;
    }

    @Override
    public boolean sendBulkSystemEmail(List<String> recipients, String subject, String body) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("Recipients list must not be null or empty");
        }
        if (subject == null) {
            throw new IllegalArgumentException("Email subject must not be null");
        }
        for (String to : recipients) {
            sendSystemEmail(to, subject, body);
        }
        return true;
    }
}
