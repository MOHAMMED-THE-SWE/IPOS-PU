package ui;

import config.DatabaseConfig;
import dao.comms.EmailOutboxDAO;
import dao.comms.PaymentLogDAO;
import dao.integration.SACommercialApplicationDAO;
import dao.integration.CAInventoryAdjustmentDAO;
import dao.members.CommercialApplicationDAO;
import dao.members.MemberApplicationDAO;
import dao.members.UserDAO;
import dao.promotions.CampaignCounterDAO;
import dao.promotions.CampaignDAO;
import dao.promotions.CampaignItemDAO;
import dao.sales.*;
import email.EmailDispatcher;
import email.EmailGatewayService;
import email.SmtpEmailService;
import integration.CommercialApplicationClient;
import integration.MerchantInventoryClient;
import model.User;
import payment.MockPaymentService;
import service.members.AuthService;
import service.members.MemberRegistrationService;
import service.promotions.CampaignCounterService;
import service.promotions.CampaignService;
import service.promotions.PricingService;
import service.reports.ReportService;
import interfaces.ICatalogueService;
import interfaces.IOrderService;
import interfaces.IPaymentService;
import service.sales.*;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    // DAOs
    // These are the data access objects that interact with the database. We create them once and pass them to services that need them.
    private final UserDAO userDAO = new UserDAO();
    private final MemberApplicationDAO memberAppDAO = new MemberApplicationDAO();
    private final CommercialApplicationDAO commercialAppDAO = new CommercialApplicationDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final OrderStatusDAO orderStatusDAO = new OrderStatusDAO();
    private final CampaignDAO campaignDAO = new CampaignDAO();
    private final CampaignItemDAO campaignItemDAO = new CampaignItemDAO();
    private final CampaignCounterDAO campaignCounterDAO = new CampaignCounterDAO();
    private final EmailOutboxDAO emailOutboxDAO = new EmailOutboxDAO();
    private final PaymentLogDAO paymentLogDAO = new PaymentLogDAO();
    // Foreign-schema DAOs — write to ipos_sa and ipos_ca on the same local MySQL instance
    private final SACommercialApplicationDAO saCommercialDAO = new SACommercialApplicationDAO();
    private final CAInventoryAdjustmentDAO caInventoryDAO = new CAInventoryAdjustmentDAO();

    // Services
    // What this means is for example create EmailGatewayService and give it the EmailOutboxDAO so it can store emails. So when someone wants to send an email,
    // it inserts a row into the email_outbox table.
    // It doesnt send the emails it literally just queues them here.
    private final EmailGatewayService emailGateway = new EmailGatewayService(emailOutboxDAO);
    // CommercialApplicationClient writes to ipos_sa.sa_commercial_applications (Team A's schema)
    private final CommercialApplicationClient commercialClient = new CommercialApplicationClient("ipos_sa", saCommercialDAO);
    // MerchantInventoryClient writes to ipos_ca.ca_inventory_adjustments (Team B's schema)
    private final MerchantInventoryClient inventoryClient = new MerchantInventoryClient("ipos_ca", caInventoryDAO);
    // We are using a FAKE payment system, it logs payments into the payment_log table.
    private final MockPaymentService paymentService = new MockPaymentService(paymentLogDAO);
    private final AuthService authService = new AuthService(userDAO);
    private final MemberRegistrationService registrationService = new MemberRegistrationService(
        userDAO, memberAppDAO, commercialAppDAO, emailGateway, commercialClient);
    private final ICatalogueService catalogueService = new CatalogueService(productDAO);
    private final CartService cartService = new CartService();
    private final PricingService pricingService = new PricingService(campaignItemDAO, userDAO);
    private final CampaignCounterService campaignCounterService = new CampaignCounterService(campaignCounterDAO, campaignItemDAO);
    // checkoutservice depends on a lot all detailed below
    private final IOrderService checkoutService = new CheckoutService(
        pricingService, paymentService, orderDAO, orderItemDAO,
        orderStatusDAO, inventoryClient, campaignCounterService, emailGateway);
    private final CampaignService campaignService = new CampaignService(campaignDAO, campaignItemDAO, campaignCounterDAO);
    private final ReportService reportService = new ReportService(campaignDAO, campaignItemDAO, campaignCounterDAO);

    // SMTP email dispatcher 
    // SMTP settings coem from database config and the integer parsing converts port 587 string to the number 587
    private final SmtpEmailService smtpService = new SmtpEmailService(
        DatabaseConfig.getProperty("smtp.host"),
        Integer.parseInt(DatabaseConfig.getProperty("smtp.port") != null ? DatabaseConfig.getProperty("smtp.port") : "587"),
        DatabaseConfig.getProperty("smtp.username"),
        DatabaseConfig.getProperty("smtp.password")
    );
    // reads unsent emails from the DB and sends them using the SMTP it runs every 60 seconds
    private final EmailDispatcher emailDispatcher = new EmailDispatcher(emailOutboxDAO, smtpService);

    private User currentUser = null;
    private final JPanel contentPanel;
    private final JLabel navUserLabel;
    private final JPanel navBar;

    // Panels created once and reused
    private final CataloguePanel cataloguePanel;
    private final CartPanel cartPanel;
    private final CheckoutPanel checkoutPanel;
    private final OrderStatusPanel ordersPanel;
    private final PromotionsPanel promotionsPanel;
    private final AdminPanel adminPanel;

    public MainFrame() {
        setTitle("IPOS-PU — Online Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        // Brand header bar — always visible at the top
        navBar = new JPanel(new BorderLayout());
        navBar.setBackground(UIConstants.BRAND_RED);
        navBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel brandLabel = new JLabel("IPOS-PU  |  Online Pharmacy Portal");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandLabel.setForeground(Color.WHITE);
        navBar.add(brandLabel, BorderLayout.WEST);

        navUserLabel = new JLabel("Not logged in");
        navUserLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        navUserLabel.setForeground(new Color(255, 220, 220));
        navBar.add(navUserLabel, BorderLayout.EAST);

        add(navBar, BorderLayout.NORTH);

        // this part is so that only one screen visible at a time
        contentPanel = new JPanel(new CardLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Now we pass the mainframe which is "this" so that it can access serices and swtich screens
        cataloguePanel  = new CataloguePanel(this);
        cartPanel       = new CartPanel(this);
        checkoutPanel   = new CheckoutPanel(this);
        ordersPanel     = new OrderStatusPanel(this);
        promotionsPanel = new PromotionsPanel(this);
        adminPanel      = new AdminPanel(this);

        // we do this here meaning for example add login screen and we name it login so later can use ShowPanel to switfch screens
        contentPanel.add(new LoginPanel(this),    "LOGIN");
        contentPanel.add(new RegisterPanel(this),  "REGISTER");
        contentPanel.add(cataloguePanel,  "CATALOGUE");
        contentPanel.add(cartPanel,       "CART");
        contentPanel.add(checkoutPanel,   "CHECKOUT");
        contentPanel.add(ordersPanel,     "ORDERS");
        contentPanel.add(promotionsPanel, "PROMOTIONS");
        contentPanel.add(adminPanel,      "ADMIN");

        showPanel("LOGIN");
        // setVisible and timer moved to start() 
    }

    public void start() {
        setVisible(true);
        // Dispatch pending emails every 60s — run on background thread, not the EDT
        new javax.swing.Timer(60_000, e -> new Thread(emailDispatcher::dispatch).start()).start();
    }

    public void showPanel(String name) {
        switch (name) {
            case "CATALOGUE"  -> cataloguePanel.refresh();
            case "CART"       -> cartPanel.refresh();
            case "CHECKOUT"   -> checkoutPanel.refresh();
            case "ORDERS"     -> ordersPanel.refresh();
            case "PROMOTIONS" -> promotionsPanel.refresh();
            case "ADMIN"      -> adminPanel.refresh();
        }
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, name);
    }

    private void refreshNavBar() {
        if (currentUser != null) {
            String role = currentUser.isAdmin() ? "Admin" : "Member";
            navUserLabel.setText(currentUser.getEmail() + "  |  " + role);
        } else {
            navUserLabel.setText("Browsing as Guest");
        }
    }

    public void onLoginSuccess(User user) {
        this.currentUser = user;
        refreshNavBar();
        if (user != null && user.isMustChangePassword()) {
            showChangePasswordDialog();
        } else {
            showPanel("CATALOGUE");
        }
    }

    private void showChangePasswordDialog() {
        JPasswordField newPassField = new JPasswordField(20);
        JPasswordField confirmPassField = new JPasswordField(20);
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("New Password (min 8 chars):"));
        panel.add(newPassField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPassField);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Change Password Required", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPass = new String(newPassField.getPassword());
            String confirm = new String(confirmPassField.getPassword());
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                showChangePasswordDialog();
                return;
            }
            try {
                authService.changePassword(currentUser.getId(), newPass);
                currentUser.setMustChangePassword(false);
                JOptionPane.showMessageDialog(this, "Password changed successfully. Welcome!", "Success", JOptionPane.INFORMATION_MESSAGE);
                showPanel("CATALOGUE");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                showChangePasswordDialog();
            }
        } else {
            // Force them to change password
            JOptionPane.showMessageDialog(this, "You must change your password before using the system.");
            showChangePasswordDialog();
        }
    }

    public void logout() {
        currentUser = null;
        cartService.clear();
        navUserLabel.setText("Not logged in");
        showPanel("LOGIN");
    }

    // Getters for child panels
    public User getCurrentUser() { return currentUser; }
    public AuthService getAuthService() { return authService; }
    public MemberRegistrationService getRegistrationService() { return registrationService; }
    public ICatalogueService getCatalogueService() { return catalogueService; }
    public CartService getCartService() { return cartService; }
    public PricingService getPricingService() { return pricingService; }
    public IOrderService getCheckoutService() { return checkoutService; }
    public CampaignService getCampaignService() { return campaignService; }
    public CampaignCounterService getCampaignCounterService() { return campaignCounterService; }
    public CampaignCounterDAO getCampaignCounterDAO() { return campaignCounterDAO; }
    public ReportService getReportService() { return reportService; }
    public OrderDAO getOrderDAO() { return orderDAO; }
    public OrderItemDAO getOrderItemDAO() { return orderItemDAO; }
    public OrderStatusDAO getOrderStatusDAO() { return orderStatusDAO; }
    public IPaymentService getPaymentService() { return paymentService; }
}
