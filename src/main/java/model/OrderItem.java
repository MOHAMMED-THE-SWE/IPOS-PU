package model;

public class OrderItem {

    private int id;
    // Which order this line belongs to (foreign key to Order.orderId).
    private String orderId;
    private int itemId;
    private int qty;
    // IMPORTANT: price per unit at the time of purchase.
    // This is saved so the order stays consistent even if product price changes later.
    private double unitPriceAtTime;
    // Discount applied to this line (e.g., 10 means 10% off).
    private double discountPercent;
    // Total cost of this line after discount
    private double lineTotal;

    public OrderItem() {}

    public OrderItem(String orderId, int itemId, int qty, double unitPriceAtTime, double discountPercent) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.qty = qty;
        this.unitPriceAtTime = unitPriceAtTime;
        this.discountPercent = discountPercent;
         // Calculates line total immediately so it’s stored consistently.
        this.lineTotal = unitPriceAtTime * qty * (1.0 - discountPercent / 100.0);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public double getUnitPriceAtTime() { return unitPriceAtTime; }
    public void setUnitPriceAtTime(double unitPriceAtTime) { this.unitPriceAtTime = unitPriceAtTime; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }

    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
}
