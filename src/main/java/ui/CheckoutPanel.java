package ui;

import model.CardDetails;
import model.Order;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class CheckoutPanel extends JPanel {

    private final MainFrame frame;
    private final JLabel totalLabel;
    private final JLabel guestEmailLabel;
    private final JTextField guestEmailField;

    public CheckoutPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Checkout — Delivery & Payment", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.BRAND_RED);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        totalLabel = new JLabel();
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 1;
        add(totalLabel, gbc);

        // Delivery address
        JTextArea addressField = new JTextArea(3, 25);
        addressField.setLineWrap(true);
        addressField.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressField);
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0; add(new JLabel("Delivery Address:"), gbc);
        gbc.gridx = 1; add(addressScroll, gbc);

        // Guest email row (row 3) — shown only when no user is logged in
        guestEmailLabel = new JLabel("Your Email (for confirmation):");
        guestEmailField = new JTextField(25);
        gbc.gridy = 3; gbc.gridx = 0; add(guestEmailLabel, gbc);
        gbc.gridx = 1; add(guestEmailField, gbc);

        // Card details (start at row 4)
        JTextField holderField = new JTextField(25);
        JTextField cardNumField = new JTextField(25);
        JTextField expiryField = new JTextField(8);
        expiryField.setToolTipText("MM/YY");
        JPasswordField cvvField = new JPasswordField(5);
        JComboBox<String> cardTypeBox = new JComboBox<>(new String[]{"VISA", "MASTERCARD", "AMEX"});

        String[][] rows = {{"Card Type:", null}, {"Card Holder Name:", null},
            {"Card Number:", null}, {"Expiry (MM/YY):", null}, {"CVV:", null}};
        Component[] fields = {cardTypeBox, holderField, cardNumField, expiryField, cvvField};

        for (int i = 0; i < rows.length; i++) {
            gbc.gridy = i + 4; gbc.gridx = 0; add(new JLabel(rows[i][0]), gbc);
            gbc.gridx = 1; add(fields[i], gbc);
        }

        JButton payBtn  = UIConstants.primaryButton("Place Order & Pay");
        JButton backBtn = UIConstants.secondaryButton("Back to Cart");
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(backBtn);
        btnPanel.add(payBtn);
        gbc.gridy = rows.length + 4; gbc.gridx = 0; gbc.gridwidth = 2;
        add(btnPanel, gbc);

        payBtn.addActionListener(e -> {
            String address = addressField.getText().trim();
            String holder = holderField.getText().trim();
            String cardNum = cardNumField.getText().trim();
            String expiry = expiryField.getText().trim();
            String cvv = new String(cvvField.getPassword()).trim();
            String cardType = (String) cardTypeBox.getSelectedItem();
            String guestEmail = guestEmailField.isVisible() ? guestEmailField.getText().trim() : null;

            if (address.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a delivery address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (frame.getCurrentUser() == null && (guestEmail == null || guestEmail.isEmpty())) {
                JOptionPane.showMessageDialog(frame, "Please enter your email address for order confirmation.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (holder.isEmpty() || cardNum.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all card details.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CardDetails card = new CardDetails(cardType, cardNum, expiry, cvv, holder);
            payBtn.setEnabled(false);
            payBtn.setText("Processing...");

            new SwingWorker<Order, Void>() {
                @Override
                protected Order doInBackground() throws Exception {
                    return frame.getCheckoutService().checkout(
                        frame.getCurrentUser(), frame.getCartService().getItems(), card, address, guestEmail);
                }
                @Override
                protected void done() {
                    payBtn.setEnabled(true);
                    payBtn.setText("Place Order & Pay");
                    try {
                        Order order = get();
                        frame.getCartService().clear();
                        boolean emailQueued = frame.getCurrentUser() != null
                            || (guestEmail != null && !guestEmail.isEmpty());
                        String emailMsg = emailQueued ? "\n\nA confirmation email has been queued." : "";
                        JOptionPane.showMessageDialog(frame,
                            "Order placed successfully!\n\nOrder ID: " + order.getOrderId() +
                            "\nTotal: £" + String.format("%.2f", order.getTotal()) +
                            "\nStatus: " + order.getStatus() + emailMsg,
                            "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
                        if (frame.getCurrentUser() != null) {
                            frame.showPanel("ORDERS");
                        } else {
                            frame.showPanel("CATALOGUE");
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(frame, "Checkout failed: " + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        backBtn.addActionListener(e -> frame.showPanel("CART"));
    }

    public void refresh() {
        boolean isGuest = frame.getCurrentUser() == null;
        guestEmailLabel.setVisible(isGuest);
        guestEmailField.setVisible(isGuest);
        if (!isGuest) guestEmailField.setText("");

        var cartItems = frame.getCartService().getItems();
        double discountedTotal = frame.getPricingService().calculateTotal(frame.getCurrentUser(), cartItems);
        totalLabel.setText(String.format("Order Total (incl. discounts): £%.2f", discountedTotal));
    }
}
