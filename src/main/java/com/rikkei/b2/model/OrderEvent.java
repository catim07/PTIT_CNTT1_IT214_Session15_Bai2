package com.rikkei.b2.model;

public class OrderEvent {
    private String eventId;
    private OrderEventType eventType;
    private String orderId;
    private String customerId;
    private String item;
    private double amount;
    private String correlationId;
    private String reason;

    public OrderEvent() {}

    public OrderEvent(String eventId, OrderEventType eventType, String orderId, String customerId, String item, double amount, String correlationId, String reason) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.orderId = orderId;
        this.customerId = customerId;
        this.item = item;
        this.amount = amount;
        this.correlationId = correlationId;
        this.reason = reason;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public OrderEventType getEventType() { return eventType; }
    public void setEventType(OrderEventType eventType) { this.eventType = eventType; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "eventType=" + eventType +
                ", orderId='" + orderId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
