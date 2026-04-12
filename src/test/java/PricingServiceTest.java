
import dao.members.UserDAO;
import dao.promotions.CampaignItemDAO;
import model.CampaignItem;
import model.Product;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.promotions.PricingService;
import service.sales.CartService.CartItem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private CampaignItemDAO campaignItemDAO;

    @Mock
    private UserDAO userDAO;

    private PricingService pricingService;

    // A few reusable products
    private final Product apple  = new Product(1, "Apple",  2.00, 100);
    private final Product banana = new Product(2, "Banana", 1.00, 100);

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(campaignItemDAO, userDAO);
    }

    // ---- getBestDiscount tests ----

    @Test
    void getBestDiscount_noActiveCampaigns_returnsZero() {
        when(campaignItemDAO.findActiveByItemId(1)).thenReturn(List.of());

        double discount = pricingService.getBestDiscount(1);

        assertEquals(0.0, discount, "No campaigns → discount should be 0%");
    }

    @Test
    void getBestDiscount_singleCampaign_returnsItsDiscount() {
        when(campaignItemDAO.findActiveByItemId(1))
            .thenReturn(List.of(new CampaignItem(10, 1, 20.0)));

        double discount = pricingService.getBestDiscount(1);

        assertEquals(20.0, discount);
    }

    @Test
    void getBestDiscount_multipleCampaigns_returnsBestDiscount() {
        when(campaignItemDAO.findActiveByItemId(1)).thenReturn(List.of(
            new CampaignItem(10, 1, 15.0),
            new CampaignItem(11, 1, 30.0),
            new CampaignItem(12, 1, 10.0)
        ));

        double discount = pricingService.getBestDiscount(1);

        assertEquals(30.0, discount, "Should pick the highest discount across overlapping campaigns");
    }

    // ---- calculateTotal tests ----

    @Test
    void calculateTotal_noDiscounts_returnsSumOfLineTotals() {
        // apple: £2.00 x 3 = £6.00, banana: £1.00 x 2 = £2.00 → £8.00
        when(campaignItemDAO.findActiveByItemId(1)).thenReturn(List.of());
        when(campaignItemDAO.findActiveByItemId(2)).thenReturn(List.of());

        List<CartItem> cart = List.of(new CartItem(apple, 3), new CartItem(banana, 2));
        double total = pricingService.calculateTotal(null, cart);

        assertEquals(8.00, total, 0.001);
    }

    @Test
    void calculateTotal_withDiscount_appliesDiscountToLine() {
        // apple: £2.00 x 2 with 20% off = £3.20
        when(campaignItemDAO.findActiveByItemId(1))
            .thenReturn(List.of(new CampaignItem(10, 1, 20.0)));

        List<CartItem> cart = List.of(new CartItem(apple, 2));
        double total = pricingService.calculateTotal(null, cart);

        assertEquals(3.20, total, 0.001);
    }

    @Test
    void calculateTotal_discountOnOneItemOnly_otherItemFullPrice() {
        // apple: 20% off; banana: no discount
        when(campaignItemDAO.findActiveByItemId(1))
            .thenReturn(List.of(new CampaignItem(10, 1, 20.0)));
        when(campaignItemDAO.findActiveByItemId(2)).thenReturn(List.of());

        // apple 1 x £2.00 x 0.80 = £1.60; banana 1 x £1.00 = £1.00 → £2.60
        List<CartItem> cart = List.of(new CartItem(apple, 1), new CartItem(banana, 1));
        double total = pricingService.calculateTotal(null, cart);

        assertEquals(2.60, total, 0.001);
    }

    @Test
    void calculateTotal_tenthOrderForMember_appliesAdditional10Percent() {
        // banana £1.00 x 1, no campaign → base £1.00
        // user has 9 prior orders → current checkout is 10th → loyalty discount → £0.90
        when(campaignItemDAO.findActiveByItemId(2)).thenReturn(List.of());

        User member = new User(42, "member@example.com", "hash", UserRole.MEMBER, false, null);
        when(userDAO.countOrders(42)).thenReturn(9);

        List<CartItem> cart = List.of(new CartItem(banana, 1));
        double total = pricingService.calculateTotal(member, cart);

        assertEquals(0.90, total, 0.001, "10th-order loyalty discount should reduce total by 10%");
    }

    @Test
    void calculateTotal_nonTenthOrderForMember_noLoyaltyDiscount() {
        when(campaignItemDAO.findActiveByItemId(2)).thenReturn(List.of());

        User member = new User(42, "member@example.com", "hash", UserRole.MEMBER, false, null);
        when(userDAO.countOrders(42)).thenReturn(7);

        List<CartItem> cart = List.of(new CartItem(banana, 1));
        double total = pricingService.calculateTotal(member, cart);

        assertEquals(1.00, total, 0.001, "Non-10th order should NOT get loyalty discount");
    }

    @Test
    void calculateTotal_adminUser_noLoyaltyDiscountEvenOnTenth() {
        // Admin role should not receive the non-commercial member discount
        when(campaignItemDAO.findActiveByItemId(1)).thenReturn(List.of());

        User admin = new User(1, "admin@ipos.com", "hash", UserRole.ADMIN, false, null);
        // userDAO.countOrders should NOT be called for non-MEMBER roles

        List<CartItem> cart = List.of(new CartItem(apple, 1));
        double total = pricingService.calculateTotal(admin, cart);

        assertEquals(2.00, total, 0.001);
        verify(userDAO, never()).countOrders(anyInt());
    }

    @Test
    void calculateTotal_nullUser_noLoyaltyDiscount() {
        when(campaignItemDAO.findActiveByItemId(1)).thenReturn(List.of());

        List<CartItem> cart = List.of(new CartItem(apple, 1));
        double total = pricingService.calculateTotal(null, cart);

        assertEquals(2.00, total, 0.001);
        verify(userDAO, never()).countOrders(anyInt());
    }
}
