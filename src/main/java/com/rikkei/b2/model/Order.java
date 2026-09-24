package com.rikkei.b2.model;

public class Order {
    private String orderId;
    private String customerId;
    private String item;
    private double amount;
    private OrderStatus status;
    private String failureReason;

    public Order() {}

    public Order(String orderId, String customerId, String item, double amount, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.item = item;
        this.amount = amount;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
