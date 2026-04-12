
import dao.comms.PaymentLogDAO;
import model.CardDetails;
import model.PaymentResult;
import model.PaymentStatus;
import payment.MockPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentLogDAO paymentLogDAO;

    private MockPaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new MockPaymentService(paymentLogDAO);
        lenient().when(paymentLogDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void charge_validCardAndPositiveAmount_returnsSuccess() {
        CardDetails card = new CardDetails("VISA", "4111111111111111", "12/27", "123", "John Doe");

        PaymentResult result = paymentService.charge(card, 99.99, "REF-001");

        assertNotNull(result, "PaymentResult should not be null");
        assertEquals(PaymentStatus.SUCCESS, result.getStatus(),
            "Payment should succeed for valid card and positive amount");
        assertEquals("REF-001", result.getReference());
        assertTrue(result.isSuccess());
        verify(paymentLogDAO, times(1)).save(any());
    }

    @Test
    void charge_nullCard_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            paymentService.charge(null, 50.00, "REF-002"),
            "Should throw IllegalArgumentException when card is null");

        verify(paymentLogDAO, never()).save(any());
    }

    @Test
    void charge_negativeAmount_throwsIllegalArgumentException() {
        CardDetails card = new CardDetails("VISA", "4111111111111111", "12/27", "123", "John Doe");

        assertThrows(IllegalArgumentException.class, () ->
            paymentService.charge(card, -10.00, "REF-003"),
            "Should throw IllegalArgumentException for negative amount");

        verify(paymentLogDAO, never()).save(any());
    }

    @Test
    void charge_zeroAmount_throwsIllegalArgumentException() {
        CardDetails card = new CardDetails("VISA", "4111111111111111", "12/27", "123", "John Doe");

        assertThrows(IllegalArgumentException.class, () ->
            paymentService.charge(card, 0.0, "REF-004"),
            "Should throw IllegalArgumentException for zero amount");
    }

    @Test
    void charge_alwaysLogsPayment() {
        CardDetails card = new CardDetails("MASTERCARD", "5500005555555559", "06/26", "456", "Jane Smith");

        paymentService.charge(card, 149.99, "REF-005");

        verify(paymentLogDAO, times(1)).save(any());
    }
}
