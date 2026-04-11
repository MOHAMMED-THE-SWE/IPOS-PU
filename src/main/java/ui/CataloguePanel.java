package ui;

import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// UIConstants provides shared fonts, colours and button factory methods

public class CataloguePanel extends JPanel {

    private final MainFrame frame;
    private final JTextField searchField = new JTextField(20);
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Product> currentProducts;

    // Dynamic UI elements updated on refresh
    private final JLabel welcomeLabel;
    private final JButton promoBtn;
    private final JButton adminBtn;
    private final JButton viewCartBtn;
    private final JButton myOrdersBtn;

    public CataloguePanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));

        // Top bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        welcomeLabel = new JLabel();
        welcomeLabel.setFont(UIConstants.LABEL_FONT);
        topPanel.add(welcomeLabel);
        topPanel.add(Box.createHorizontalStrut(12));
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UIConstants.LABEL_FONT);
        topPanel.add(searchLbl);
        searchField.putClientProperty("JTextField.placeholderText", "Search products...");
        topPanel.add(searchField);
        JButton searchBtn = UIConstants.secondaryButton("Search");
        JButton clearBtn  = UIConstants.secondaryButton("Clear");
        topPanel.add(searchBtn);
        topPanel.add(clearBtn);

        promoBtn = UIConstants.primaryButton("★ Promotions");
        promoBtn.addActionListener(e -> frame.showPanel("PROMOTIONS"));
        topPanel.add(promoBtn);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new String[]{"Item ID", "Description", "Unit Price (£)", "Stock"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        UIConstants.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        JButton addToCartBtn = UIConstants.primaryButton("Add to Cart");
        viewCartBtn = UIConstants.secondaryButton("View Cart (0)");
        JButton logoutBtn = UIConstants.secondaryButton("Logout");
        adminBtn = UIConstants.secondaryButton("Admin Panel");
        adminBtn.addActionListener(e -> frame.showPanel("ADMIN"));
        myOrdersBtn = UIConstants.secondaryButton("My Orders");
        myOrdersBtn.addActionListener(e -> frame.showPanel("ORDERS"));
        bottomPanel.add(adminBtn);
        bottomPanel.add(myOrdersBtn);
        JLabel qtyLbl = new JLabel("Qty:");
        qtyLbl.setFont(UIConstants.LABEL_FONT);
        bottomPanel.add(qtyLbl);
        bottomPanel.add(qtySpinner);
        bottomPanel.add(addToCartBtn);
        bottomPanel.add(viewCartBtn);
        bottomPanel.add(logoutBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load all products
        loadProducts(null);

        searchBtn.addActionListener(e -> loadProducts(searchField.getText()));
        clearBtn.addActionListener(e -> { searchField.setText(""); loadProducts(null); });
        searchField.addActionListener(e -> loadProducts(searchField.getText()));

        addToCartBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a product first."); return; }
            Product product = currentProducts.get(row);
            int qty = (Integer) qtySpinner.getValue();
            frame.getCartService().addItem(product, qty);
            viewCartBtn.setText("View Cart (" + frame.getCartService().getItems().size() + ")");

            // Record campaign item_hits_qty if applicable
            frame.getCampaignService().getActiveCampaigns().forEach(campaign -> {
                frame.getCampaignService().getCampaignItems(campaign.getCampaignId()).forEach(ci -> {
                    if (ci.getItemId() == product.getItemId()) {
                        frame.getCampaignCounterService().recordItemAdded(campaign.getCampaignId(), product.getItemId(), qty);
                    }
                });
            });

            JOptionPane.showMessageDialog(this,
                product.getDescription() + " x" + qty + " added to cart.", "Added to Cart",
                JOptionPane.INFORMATION_MESSAGE);
        });

        viewCartBtn.addActionListener(e -> frame.showPanel("CART"));
        logoutBtn.addActionListener(e -> frame.logout());
    }

    public void refresh() {
        welcomeLabel.setText("IPOS-PU Catalogue  |  Welcome, " +
            (frame.getCurrentUser() != null ? frame.getCurrentUser().getEmail() : "Guest"));
        promoBtn.setVisible(frame.getCampaignService().hasActiveCampaigns());
        adminBtn.setVisible(frame.getCurrentUser() != null && frame.getCurrentUser().isAdmin());
        myOrdersBtn.setVisible(frame.getCurrentUser() != null && !frame.getCurrentUser().isAdmin());
        viewCartBtn.setText("View Cart (" + frame.getCartService().getItems().size() + ")");
        searchField.setText("");
        loadProducts(null);
    }

    private void loadProducts(String keyword) {
        currentProducts = (keyword == null || keyword.isBlank())
            ? frame.getCatalogueService().getAll()
            : frame.getCatalogueService().search(keyword);

        tableModel.setRowCount(0);
        for (Product p : currentProducts) {
            tableModel.addRow(new Object[]{
                p.getItemId(),
                p.getDescription(),
                String.format("£%.2f", p.getUnitPrice()),
                p.getStockQty()
            });
        }
    }
}
