package ui;

import service.sales.CartService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CartPanel extends JPanel {

    private final MainFrame frame;
    private final DefaultTableModel tableModel;
    private final JLabel totalLabel;

    public CartPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Shopping Cart", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.BRAND_RED);
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 4, 0));
        add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"Item ID", "Description", "Qty", "Unit Price (£)", "Subtotal (£)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        UIConstants.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        List<CartService.CartItem> items = frame.getCartService().getItems();
        double total = 0;
        for (CartService.CartItem ci : items) {
            tableModel.addRow(new Object[]{
                ci.getProduct().getItemId(),
                ci.getProduct().getDescription(),
                ci.getQty(),
                String.format("£%.2f", ci.getProduct().getUnitPrice()),
                String.format("£%.2f", ci.getSubtotal())
            });
            total += ci.getSubtotal();
        }

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        totalLabel = new JLabel("Subtotal: £" + String.format("%.2f", total));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JButton checkoutBtn = UIConstants.primaryButton("Proceed to Checkout");
        JButton continueBtn = UIConstants.secondaryButton("Continue Shopping");
        JButton clearBtn    = UIConstants.secondaryButton("Clear Cart");
        clearBtn.setForeground(UIConstants.BRAND_RED);

        south.add(totalLabel);
        south.add(continueBtn);
        south.add(clearBtn);
        south.add(checkoutBtn);
        add(south, BorderLayout.SOUTH);

        checkoutBtn.addActionListener(e -> {
            if (frame.getCartService().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Your cart is empty.");
                return;
            }
            frame.showPanel("CHECKOUT");
        });

        continueBtn.addActionListener(e -> frame.showPanel("CATALOGUE"));
        clearBtn.addActionListener(e -> {
            frame.getCartService().clear();
            frame.showPanel("CATALOGUE");
        });
    }

    public void refresh() {
        tableModel.setRowCount(0);
        double total = 0;
        for (CartService.CartItem ci : frame.getCartService().getItems()) {
            tableModel.addRow(new Object[]{
                ci.getProduct().getItemId(),
                ci.getProduct().getDescription(),
                ci.getQty(),
                String.format("£%.2f", ci.getProduct().getUnitPrice()),
                String.format("£%.2f", ci.getSubtotal())
            });
            total += ci.getSubtotal();
        }
        totalLabel.setText("Subtotal: £" + String.format("%.2f", total));
    }
}
