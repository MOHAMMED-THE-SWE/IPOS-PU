package email;

import dao.comms.EmailOutboxDAO;
import interfaces.IEmailService;
import model.EmailOutbox;
import model.EmailStatus;

import java.util.List;

public class EmailDispatcher {

    private final EmailOutboxDAO emailOutboxDAO;
    private final IEmailService emailService;

    public EmailDispatcher(EmailOutboxDAO emailOutboxDAO, IEmailService emailService) {
        this.emailOutboxDAO = emailOutboxDAO;
        this.emailService = emailService;
    }

    /**
     * Processes all PENDING emails in the outbox.
     *
     * @return number of emails successfully sent
     */
    public int dispatch() {
        List<EmailOutbox> pending = emailOutboxDAO.findPending();
        int sent = 0;
        for (EmailOutbox email : pending) {
            try {
                emailService.sendEmail(email.getToEmail(), email.getSubject(), email.getBody());
                emailOutboxDAO.updateStatus(email.getId(), EmailStatus.SENT, null);
                sent++;
            } catch (Exception e) {
                emailOutboxDAO.updateStatus(email.getId(), EmailStatus.FAILED,
                    e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        return sent;
    }
}
