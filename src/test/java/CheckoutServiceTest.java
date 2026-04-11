
import dao.sales.OrderDAO;
import dao.sales.OrderItemDAO;
import dao.sales.OrderStatusDAO;
import interfaces.IEmailGateway;
import interfaces.IMerchantInventoryService;
import interfaces.IPaymentService;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.promotions.CampaignCounterService;
import service.promotions.PricingService;
import service.sales.CartService.CartItem;
import service.sales.CheckoutService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private PricingService pricingService;
    @Mock private IPaymentService paymentService;
    @Mock private OrderDAO orderDAO;
    @Mock private OrderItemDAO orderItemDAO;
    @Mock private OrderStatusDAO orderStatusDAO;
    @Mock private IMerchantInventoryService inventoryService;
    @Mock private CampaignCounterService campaignCounterService;
    @Mock private IEmailGateway emailGateway;

    private CheckoutService checkoutService;

    private final User testUser = new User(1, "user@example.com", "hash", UserRole.MEMBER, false, null);
    private final CardDetails validCard = new CardDetails("VISA", "4111111111111111", "12/27", "123", "Test User");
    private final Product prod = new Product(10, "Widget", 10.00, 50);

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(
            pricingService, paymentService, orderDAO, orderItemDAO,
            orderStatusDAO, inventoryService, campaignCounterService, emailGateway);
    }

    @Test
    void checkout_successPath_returnsOrderWithReceivedStatus() {
        List<CartItem> cart = List.of(new CartItem(prod, 2));

        when(pricingService.calculateTotal(any(), anyList())).thenReturn(20.00);
        when(pricingService.getBestDiscount(prod.getItemId())).thenReturn(0.0);
        when(paymentService.charge(any(), anyDouble(), anyString()))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCESS, "REF-001", "OK", PaymentProvider.MOCK));
        when(inventoryService.deductStock(anyString(), anyList())).thenReturn(true);
        when(emailGateway.sendSystemEmail(anyString(), anyString(), anyString())).thenReturn(true);

        Order order = checkoutService.checkout(testUser, cart, validCard, "123 Test Street, London", null);

        assertNotNull(order);
        assertEquals(20.00, order.getTotal(), 0.001);
        assertEquals(OrderStatus.RECEIVED, order.getStatus());
    }

    @Test
    void checkout_successPath_chargesPaymentExactlyOnce() {
        List<CartItem> cart = List.of(new CartItem(prod, 1));

        when(pricingService.calculateTotal(any(), anyList())).thenReturn(10.00);
        when(pricingService.getBestDiscount(prod.getItemId())).thenReturn(0.0);
        when(paymentService.charge(any(), anyDouble(), anyString()))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCESS, "REF-X", "OK", PaymentProvider.MOCK));
        when(inventoryService.deductStock(anyString(), anyList())).thenReturn(true);
        when(emailGateway.sendSystemEmail(anyString(), anyString(), anyString())).thenReturn(true);

        checkoutService.checkout(testUser, cart, validCard, "123 Test Street, London", null);

        verify(paymentService, times(1)).charge(eq(validCard), eq(10.00), anyString());
    }

    @Test
    void checkout_successPath_savesOrderAndSendsEmail() {
        List<CartItem> cart = List.of(new CartItem(prod, 1));

        when(pricingService.calculateTotal(any(), anyList())).thenReturn(10.00);
        when(pricingService.getBestDiscount(prod.getItemId())).thenReturn(0.0);
        when(paymentService.charge(any(), anyDouble(), anyString()))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCESS, "REF-Y", "OK", PaymentProvider.MOCK));
        when(inventoryService.deductStock(anyString(), anyList())).thenReturn(true);
        when(emailGateway.sendSystemEmail(anyString(), anyString(), anyString())).thenReturn(true);

        checkoutService.checkout(testUser, cart, validCard, "123 Test Street, London", null);

        verify(orderDAO, times(1)).save(any(Order.class));
        verify(emailGateway, times(1)).sendSystemEmail(eq(testUser.getEmail()), anyString(), anyString());
    }

    @Test
    void checkout_paymentFails_throwsRuntimeExceptionAndOrderNotSaved() {
        List<CartItem> cart = List.of(new CartItem(prod, 1));

        when(pricingService.calculateTotal(any(), anyList())).thenReturn(10.00);
        when(paymentService.charge(any(), anyDouble(), anyString()))
            .thenReturn(new PaymentResult(PaymentStatus.FAILED, "REF-Z", "Declined", PaymentProvider.MOCK));

        assertThrows(RuntimeException.class, () ->
            checkoutService.checkout(testUser, cart, validCard, "123 Test Street, London", null),
            "Payment failure should throw RuntimeException");

        verify(orderDAO, never()).save(any());
        verify(emailGateway, never()).sendSystemEmail(anyString(), anyString(), anyString());
    }

    @Test
    void checkout_emptyCart_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            checkoutService.checkout(testUser, List.of(), validCard, "123 Test Street, London", null),
            "Empty cart should throw IllegalArgumentException");

        verifyNoInteractions(paymentService, orderDAO);
    }

    @Test
    void checkout_nullCart_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            checkoutService.checkout(testUser, null, validCard, "123 Test Street, London", null));

        verifyNoInteractions(paymentService, orderDAO);
    }

    @Test
    void checkout_nullCard_throwsIllegalArgumentException() {
        List<CartItem> cart = List.of(new CartItem(prod, 1));

        assertThrows(IllegalArgumentException.class, () ->
            checkoutService.checkout(testUser, cart, null, "123 Test Street, London", null),
            "Null card should throw IllegalArgumentException");

        verifyNoInteractions(paymentService, orderDAO);
    }

    @Test
    void checkout_nullUser_stillCompletesWithoutEmail() {
        List<CartItem> cart = List.of(new CartItem(prod, 1));

        when(pricingService.calculateTotal(isNull(), anyList())).thenReturn(10.00);
        when(pricingService.getBestDiscount(prod.getItemId())).thenReturn(0.0);
        when(paymentService.charge(any(), anyDouble(), anyString()))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCESS, "REF-NULL", "OK", PaymentProvider.MOCK));
        when(inventoryService.deductStock(anyString(), anyList())).thenReturn(true);

        Order order = checkoutService.checkout(null, cart, validCard, "123 Test Street, London", null);

        assertNotNull(order);
        // No email sent when user is null
        verify(emailGateway, never()).sendSystemEmail(anyString(), anyString(), anyString());
    }
}
