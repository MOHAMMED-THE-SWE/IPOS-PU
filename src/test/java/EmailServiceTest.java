
import email.SmtpEmailService;
import interfaces.IEmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for IEmailService / SmtpEmailService.
 *
 * Real SMTP connections are NOT made here — doing so would make the suite
 * slow, flaky, and network-dependent. Instead we verify:
 *  1. The constructor accepts the four required parameters (interface contract).
 *  2. SmtpEmailService implements IEmailService (type / LSP check).
 *  3. A stub simulates MessagingException propagation without any I/O.
 */
class EmailServiceTest {

    // ---- Constructor / type contract ----

    @Test
    void smtpEmailService_constructorAcceptsFourParams() {
        assertDoesNotThrow(() ->
            new SmtpEmailService("smtp.example.com", 587, "user@example.com", "secret"),
            "Constructor should accept host, port, username, password without throwing");
    }

    @Test
    void smtpEmailService_implementsIEmailService() {
        SmtpEmailService service = new SmtpEmailService("smtp.gmail.com", 587, "user@gmail.com", "apppassword");
        assertInstanceOf(IEmailService.class, service,
            "SmtpEmailService must implement IEmailService");
    }

    // ---- Exception propagation (no real network) ----

    /**
     * Minimal stub that simulates an SMTP transport failure.
     * Tests the IEmailService contract: sendEmail must declare/propagate MessagingException.
     */
    static class FailingEmailService implements IEmailService {
        @Override
        public boolean sendEmail(String to, String subject, String body) throws MessagingException {
            throw new MessagingException("Simulated SMTP failure — no real connection made");
        }
    }

    @Test
    void iEmailService_sendEmail_propagatesMessagingExceptionOnFailure() {
        IEmailService failingService = new FailingEmailService();

        assertThrows(MessagingException.class, () ->
            failingService.sendEmail("to@example.com", "Subject", "Body"),
            "IEmailService.sendEmail must propagate MessagingException on transport failure");
    }
}
