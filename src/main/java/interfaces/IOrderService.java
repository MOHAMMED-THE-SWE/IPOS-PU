package interfaces;

import model.CardDetails;
import model.Order;
import model.OrderStatus;
import model.User;
import service.sales.CartService.CartItem;

import java.util.List;

/**
 * Manages the online order lifecycle for IPOS-PU.
 *
 * <p>Implemented by {@code service.sales.CheckoutService}. This interface exposes
 * the order placement and status update contract, allowing other subsystems and
 * UI components to depend on the abstraction rather than the concrete service.</p>
 *
 * @author Team C
 */
public interface IOrderService {

    /**
     * Processes a full checkout: calculates the total (including campaign discounts
     * and the 10th-order member discount), charges the card, persists the order,
     * propagates stock deductions to IPOS-CA, and queues a confirmation email.
     *
     * @param user            the logged-in user (null for a guest checkout)
     * @param cartItems       the items in the cart (must not be null or empty)
     * @param card            the payment card details (must not be null)
     * @param deliveryAddress the shipping address for the order (must not be blank)
     * @param guestEmail      email address for guest confirmation (used when user is null; may be null/blank to skip)
     * @return the created {@link Order} with status {@code RECEIVED}
     * @throws IllegalArgumentException if the cart is empty, card is null, or address is blank
     * @throws RuntimeException         if payment is declined
     */
    Order checkout(User user, List<CartItem> cartItems, CardDetails card, String deliveryAddress, String guestEmail);

    /**
     * Updates the status of an existing order and records the change in the status history.
     *
     * @param orderId   the unique order identifier
     * @param newStatus the new status to apply (e.g. DISPATCHED, DELIVERED)
     * @param note      an optional note describing the reason for the status change
     */
    void updateOrderStatus(String orderId, OrderStatus newStatus, String note);
}
