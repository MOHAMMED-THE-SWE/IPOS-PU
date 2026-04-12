
import dao.comms.EmailOutboxDAO;
import email.EmailGatewayService;
import model.EmailOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailGatewayServiceTest {

    @Mock
    private EmailOutboxDAO emailOutboxDAO;

    private EmailGatewayService emailGatewayService;

    @BeforeEach
    void setUp() {
        emailGatewayService = new EmailGatewayService(emailOutboxDAO);
    }

    @Test
    void sendSystemEmail_validInputs_returnsTrue() {
        when(emailOutboxDAO.save(any(EmailOutbox.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = emailGatewayService.sendSystemEmail(
            "user@example.com", "Welcome", "Your account has been created.");

        assertTrue(result, "Should return true for valid inputs");
        verify(emailOutboxDAO, times(1)).save(any(EmailOutbox.class));
    }

    @Test
    void sendSystemEmail_invalidEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            emailGatewayService.sendSystemEmail("not-an-email", "Subject", "Body"),
            "Should throw IllegalArgumentException for invalid email");

        verify(emailOutboxDAO, never()).save(any());
    }

    @Test
    void sendSystemEmail_nullEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            emailGatewayService.sendSystemEmail(null, "Subject", "Body"),
            "Should throw IllegalArgumentException for null email");
    }

    @Test
    void sendSystemEmail_nullSubject_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            emailGatewayService.sendSystemEmail("user@example.com", null, "Body"),
            "Should throw IllegalArgumentException for null subject");

        verify(emailOutboxDAO, never()).save(any());
    }

    @Test
    void sendSystemEmail_emptySubject_doesNotThrow() {
        when(emailOutboxDAO.save(any(EmailOutbox.class))).thenAnswer(inv -> inv.getArgument(0));

        // Empty string subject is allowed (not null)
        boolean result = emailGatewayService.sendSystemEmail("user@example.com", "", "Body");
        assertTrue(result);
    }

    // ---- sendBulkSystemEmail ----

    @Test
    void sendBulkSystemEmail_validRecipients_savesOnePerRecipient() {
        when(emailOutboxDAO.save(any(EmailOutbox.class))).thenAnswer(inv -> inv.getArgument(0));

        List<String> recipients = List.of("a@example.com", "b@example.com", "c@example.com");
        boolean result = emailGatewayService.sendBulkSystemEmail(recipients, "Newsletter", "Body text");

        assertTrue(result, "Should return true for valid bulk send");
        verify(emailOutboxDAO, times(3)).save(any(EmailOutbox.class));
    }

    @Test
    void sendBulkSystemEmail_nullRecipients_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            emailGatewayService.sendBulkSystemEmail(null, "Subject", "Body"),
            "Should throw IllegalArgumentException for null recipients");

        verify(emailOutboxDAO, never()).save(any());
    }

    @Test
    void sendBulkSystemEmail_emptyRecipientList_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            emailGatewayService.sendBulkSystemEmail(List.of(), "Subject", "Body"),
            "Should throw IllegalArgumentException for empty recipients list");

        verify(emailOutboxDAO, never()).save(any());
    }

    @Test
    void sendBulkSystemEmail_invalidEmailInList_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            emailGatewayService.sendBulkSystemEmail(
                List.of("valid@example.com", "not-an-email"), "Subject", "Body"),
            "Should throw IllegalArgumentException when any address is invalid");
    }

    @Test
    void sendBulkSystemEmail_nullSubject_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            emailGatewayService.sendBulkSystemEmail(List.of("a@example.com"), null, "Body"),
            "Should throw IllegalArgumentException for null subject");

        verify(emailOutboxDAO, never()).save(any());
    }
}
