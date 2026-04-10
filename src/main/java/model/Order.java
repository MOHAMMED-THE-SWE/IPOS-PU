package model;

import java.time.LocalDateTime;

//Order
//Represents the order header (not the items inside it).
//Uses OrderStatus.RECEIVED by default in the constructor.
//total is for the whole order (line totals are in OrderItem).

public class Order {

    private String orderId;
    // Which user placed the order (links to users table).
    private Integer userId;
    private OrderStatus status;
    private double total;
    private String deliveryAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {}

    public Order(String orderId, Integer userId, double total, String deliveryAddress) {
        this.orderId = orderId;
        this.userId = userId;
        this.total = total;
        this.deliveryAddress = deliveryAddress;
        this.status = OrderStatus.RECEIVED;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
