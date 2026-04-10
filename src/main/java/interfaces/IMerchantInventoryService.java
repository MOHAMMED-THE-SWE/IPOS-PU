package interfaces;

import model.OrderItem;

import java.util.List;

/**
 * IMerchantInventoryService — PU depends on this to propagate stock
 * deductions to IPOS-CA after successful online orders.
 */
public interface IMerchantInventoryService {

    /**
     * Instructs CA to deduct stock for the given order items.
     *
     * @param orderId the unique order identifier (must not be null)
     * @param items   the list of items to deduct (must not be null or empty)
     * @return true if the deduction request was successfully submitted
     * @throws IllegalArgumentException if orderId is null or items is null/empty
     */
    boolean deductStock(String orderId, List<OrderItem> items);

    /**
     * Cancels a pending stock deduction request for the given order.
     * Called when an order is cancelled before CA has processed the deduction.
     *
     * @param orderId the unique order identifier (must not be null or blank)
     * @return true if the cancellation request was successfully submitted
     * @throws IllegalArgumentException if orderId is null or blank
     */
    boolean cancelStockDeduction(String orderId);
}
