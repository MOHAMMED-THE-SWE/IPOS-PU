package payment;

import dao.comms.PaymentLogDAO;
import interfaces.IPaymentService;
import model.*;

/**
 * Mock implementation of {@link IPaymentService} for IPOS-PU-COMMS.
 *
 * <p>Simulates a payment processor by always returning {@code SUCCESS} for any valid
 * card and positive amount. All charge attempts — successful or rejected due to
 * validation — are logged to {@code pu_payment_log} with provider {@code MOCK}.
 * This ensures the payment audit trail is maintained even when the PayPal Sandbox
 * integration is not in use.</p>
 *
 * @author Team C
 */
public class MockPaymentService implements IPaymentService {

    private final PaymentLogDAO paymentLogDAO;

    /**
     * Creates a new MockPaymentService.
     *
     * @param paymentLogDAO DAO used to persist payment log entries to {@code pu_payment_log}
     */
    public MockPaymentService(PaymentLogDAO paymentLogDAO) {
        this.paymentLogDAO = paymentLogDAO;
    }

    @Override
    public PaymentResult charge(CardDetails card, double amount, String reference) {
        if (card == null) {
            throw new IllegalArgumentException("Card details must not be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        // Cards starting with "0000" simulate a declined card for demo purposes
        boolean declined = card.getCardNumber() != null && card.getCardNumber().startsWith("9999");
        PaymentStatus status = declined ? PaymentStatus.FAILED : PaymentStatus.SUCCESS;
        String message = declined ? "Card declined (test card)" : "Mock payment approved";

        PaymentResult result = new PaymentResult(status, reference, message, PaymentProvider.MOCK);

        // Log the payment attempt regardless of outcome
        PaymentLog log = new PaymentLog(
            "MOCK",
            reference,
            amount,
            card.getCardType(),
            card.getFirst4(),
            card.getLast4(),
            card.getExpiry(),
            status,
            PaymentProvider.MOCK
        );
        paymentLogDAO.save(log);

        return result;
    }
}
