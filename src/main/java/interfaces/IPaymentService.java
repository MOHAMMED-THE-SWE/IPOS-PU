package interfaces;

import model.CardDetails;
import model.PaymentResult;

/**
 * IPaymentService — PU depends on this to charge customers during checkout.
 * Implementations: MockPaymentService (default) or PayPalPaymentService (optional).
 */
public interface IPaymentService {

    /**
     * Charges the given card for the specified amount.
     *
     * @param card      card details (must not be null)
     * @param amount    amount to charge in GBP (must be > 0)
     * @param reference unique order reference string
     * @return PaymentResult indicating SUCCESS or FAILED
     * @throws IllegalArgumentException if card is null or amount <= 0
     */
    PaymentResult charge(CardDetails card, double amount, String reference);
}
