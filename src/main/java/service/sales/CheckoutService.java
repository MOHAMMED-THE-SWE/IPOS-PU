package service.sales;

import dao.sales.OrderDAO;
import dao.sales.OrderItemDAO;
import dao.sales.OrderStatusDAO;
import interfaces.IEmailGateway;
import interfaces.IMerchantInventoryService;
import interfaces.IOrderService;
import interfaces.IPaymentService;
import model.CardDetails;
import model.Order;
import model.OrderItem;
import model.OrderStatus;
import model.OrderStatusHistory;
import model.PaymentResult;
import model.User;
import service.promotions.CampaignCounterService;
import service.promotions.PricingService;
import service.sales.CartService.CartItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the full online checkout workflow for IPOS-PU-Sales.
 *
 * <p>Implements {@link IOrderService}. Responsibilities:</p>
 * <ol>
 *   <li>Calculates the order total via {@link service.promotions.PricingService}
 *       (applying campaign discounts and the 10th-order member discount).</li>
 *   <li>Charges the customer's card via {@link interfaces.IPaymentService}.</li>
 *   <li>Persists the order header and line items to the database.</li>
 *   <li>Records the initial {@code RECEIVED} status in the order history.</li>
 *   <li>Propagates stock deductions to IPOS-CA via {@link interfaces.IMerchantInventoryService}.</li>
 *   <li>Increments campaign purchase counters.</li>
 *   <li>Queues an order confirmation email via {@link interfaces.IEmailGateway}.</li>
 * </ol>
 *
 * @author Team C
 */
public class CheckoutService implements IOrderService {

    private final PricingService pricingService;
    private final IPaymentService paymentService;
    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final OrderStatusDAO orderStatusDAO;
    private final IMerchantInventoryService inventoryService;
    private final CampaignCounterService campaignCounterService;
    private final IEmailGateway emailGateway;

    /**
     * Creates a new CheckoutService with all required dependencies.
     *
     * @param pricingService         calculates discounted totals
     * @param paymentService         charges the customer's card
     * @param orderDAO               persists order headers
     * @param orderItemDAO           persists order line items
     * @param orderStatusDAO         persists order status history entries
     * @param inventoryService       propagates stock deductions to IPOS-CA
     * @param campaignCounterService increments campaign hit and purchase counters
     * @param emailGateway           queues order confirmation emails
     */
    public CheckoutService(PricingService pricingService,
                           IPaymentService paymentService,
                           OrderDAO orderDAO,
                           OrderItemDAO orderItemDAO,
                           OrderStatusDAO orderStatusDAO,
                           IMerchantInventoryService inventoryService,
                           CampaignCounterService campaignCounterService,
                           IEmailGateway emailGateway) {
        this.pricingService = pricingService;
        this.paymentService = paymentService;
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
        this.orderStatusDAO = orderStatusDAO;
        this.inventoryService = inventoryService;
        this.campaignCounterService = campaignCounterService;
        this.emailGateway = emailGateway;
    }

    @Override
    public Order checkout(User user, List<CartItem> cartItems, CardDetails card, String deliveryAddress, String guestEmail) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        if (card == null) {
            throw new IllegalArgumentException("Card details required");
        }
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("Delivery address required");
        }

        // calc total including any discounts
        double total = pricingService.calculateTotal(user, cartItems);

        // take payment
        String reference = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PaymentResult paymentResult = paymentService.charge(card, total, reference);

        if (!paymentResult.isSuccess()) {
            throw new RuntimeException("Payment failed: " + paymentResult.getMessage());
        }

        String orderId = reference;
        Order order = new Order(orderId, user != null ? user.getId() : null, total, deliveryAddress);
        orderDAO.save(order);

        // add items to the order
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cartItems) {
            double discount = pricingService.getBestDiscount(ci.getProduct().getItemId());
            OrderItem oi = new OrderItem(orderId, ci.getProduct().getItemId(),
                ci.getQty(), ci.getProduct().getUnitPrice(), discount);
            orderItems.add(oi);
        }
        orderItemDAO.saveAll(orderItems);

        // record initial status in history
        orderStatusDAO.save(new OrderStatusHistory(orderId, OrderStatus.RECEIVED, "Order placed online"));

        // deduct stock in CA
        inventoryService.deductStock(orderId, orderItems);

        campaignCounterService.recordPurchase(cartItems);

        // email the customer (logged-in member or guest who provided an email)
        String emailTo = user != null ? user.getEmail()
                       : (guestEmail != null && !guestEmail.isBlank()) ? guestEmail.trim() : null;
        if (emailTo != null) {
            String subject = "Order Confirmed - " + orderId;
            String body = buildConfirmationEmail(emailTo, order, cartItems, total);
            emailGateway.sendSystemEmail(emailTo, subject, body);
        }

        return order;
    }

    @Override
    public void updateOrderStatus(String orderId, OrderStatus newStatus, String note) {
        orderDAO.updateStatus(orderId, newStatus);
        orderStatusDAO.save(new OrderStatusHistory(orderId, newStatus, note));
    }

    private String buildConfirmationEmail(String emailTo, Order order, List<CartItem> cartItems, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(emailTo).append(",\n\n");
        sb.append("Thank you for your order!\n\n");
        sb.append("Order Reference: ").append(order.getOrderId()).append("\n");
        sb.append("Order Date: ").append(LocalDateTime.now()).append("\n\n");
        sb.append("Items Ordered:\n");
        for (CartItem ci : cartItems) {
            sb.append("  - ").append(ci.getProduct().getDescription())
              .append(" x").append(ci.getQty())
              .append(" @ £").append(String.format("%.2f", ci.getProduct().getUnitPrice()))
              .append("\n");
        }
        sb.append("\nTotal: £").append(String.format("%.2f", total)).append("\n");
        sb.append("Delivery Address: ").append(order.getDeliveryAddress()).append("\n\n");
        sb.append("Tracking: Your order status is RECEIVED.\n");
        sb.append("You can track your order by logging into IPOS-PU and viewing 'My Orders'.\n\n");
        sb.append("Thank you for shopping with IPOS-PU!");
        return sb.toString();
    }
}
