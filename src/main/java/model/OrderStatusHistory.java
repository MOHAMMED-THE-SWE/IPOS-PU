package model;

import java.time.LocalDateTime;

public class OrderStatusHistory {

    private int id;
    // Which order this history entry belongs to.
    private String orderId;
    // The status value recorded in this history entry.
    private OrderStatus status;
    private LocalDateTime changedAt;
    private String note;

    public OrderStatusHistory() {}

    public OrderStatusHistory(String orderId, OrderStatus status, String note) {
        this.orderId = orderId;
        this.status = status;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
