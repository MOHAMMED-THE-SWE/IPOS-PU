package service.promotions;

import dao.members.UserDAO;
import dao.promotions.CampaignItemDAO;
import model.CampaignItem;
import model.User;
import model.UserRole;
import service.sales.CartService.CartItem;

import java.util.List;

/**
 * Calculates order totals and applies promotional discounts for IPOS-PU.
 *
 * <p>Two discount rules are applied at checkout:</p>
 * <ol>
 *   <li><b>Campaign discount</b> — the best active campaign discount for each line item.</li>
 *   <li><b>10th-order discount</b> — non-commercial members receive 10% off their every
 *       10th completed order (i.e., when their previous order count mod 10 equals 9).</li>
 * </ol>
 *
 * @author Team C
 */
public class PricingService {

    private final CampaignItemDAO campaignItemDAO;
    private UserDAO userDAO;

    /**
     * Creates a new PricingService.
     *
     * @param campaignItemDAO DAO for querying active campaign discounts per product
     * @param userDAO         DAO for counting a user's previous orders (for 10th-order discount)
     */
    public PricingService(CampaignItemDAO campaignItemDAO, UserDAO userDAO) {
        this.campaignItemDAO = campaignItemDAO;
        this.userDAO = userDAO;
    }

    /**
     * Calculates the total order price, applying all applicable discounts.
     *
     * <p>For each cart item, the best active campaign discount is applied.
     * If the user is a non-commercial member and this would be their 10th order,
     * an additional 10% is applied to the whole order total.</p>
     *
     * @param user      the logged-in user (may be null for guest — no member discount applied)
     * @param cartItems the items in the cart
     * @return the total order value rounded to two decimal places
     */
    public double calculateTotal(User user, List<CartItem> cartItems) {
        double total = 0.0;
        for (CartItem ci : cartItems) {
            double discount = getBestDiscount(ci.getProduct().getItemId());
            double lineTotal = ci.getProduct().getUnitPrice() * ci.getQty() * (1.0 - discount / 100.0);
            total += lineTotal;
        }

        // Apply 10% discount on every 10th order for non-commercial members
        if (user != null && user.getRole() == UserRole.MEMBER) {
            int orderCount = userDAO.countOrders(user.getId());
            if (orderCount % 10 == 9) {
                total *= 0.90;
            }
        }

        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Returns the highest discount percentage currently available for a product
     * across all active campaigns.
     *
     * @param itemId the product ID to check
     * @return the best discount percentage (0.0 if no active campaign covers this product)
     */
    public double getBestDiscount(int itemId) {
        List<CampaignItem> items = campaignItemDAO.findActiveByItemId(itemId);
        double best = 0.0;
        for (CampaignItem ci : items) {
            if (ci.getDiscountPercent() > best) {
                best = ci.getDiscountPercent();
            }
        }
        return best;
    }
}

