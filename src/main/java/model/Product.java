package model;

//Product is used:
//DAO layer (ProductDAO): reads products from MySQL which then creates Product objects
//Service layer: uses Product objects to: show catalogue, calculate totlas, validate stock

public class Product {

    private int itemId;
    private String description;
    private double unitPrice;
    private int stockQty;

    public Product() {}

    public Product(int itemId, String description, double unitPrice, int stockQty) {
        this.itemId = itemId;
        this.description = description;
        this.unitPrice = unitPrice;
        this.stockQty = stockQty;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    // This controls how a Product looks when Java needs it as text
    // (e.g., when you put Product objects into a JList or print them).
    // Example output: "[12] Paracetamol 500mg - £3.99"
    @Override
    public String toString() {
        return String.format("[%d] %s - £%.2f", itemId, description, unitPrice);
    }
}
