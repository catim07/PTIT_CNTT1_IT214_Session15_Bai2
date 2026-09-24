package com.rikkei.b2;

import com.rikkei.b2.model.Order;
import com.rikkei.b2.model.OrderStatus;
import com.rikkei.b2.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChoreographyTracingTest {

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("Test Successful Order Choreography Saga with Correlation ID Tracing")
    void testSuccessfulChoreographySaga() {
        Order order = orderService.createOrder("CUST-001", "Gaming Laptop", 1500.0);
        assertNotNull(order);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertNull(order.getFailureReason());
    }

    @Test
    @DisplayName("Test Payment Failure Choreography Saga Tracing")
    void testPaymentFailureChoreographySaga() {
        Order order = orderService.createOrder("FAIL_PAYMENT", "Wireless Mouse", 50.0);
        assertNotNull(order);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertNotNull(order.getFailureReason());
        assertTrue(order.getFailureReason().toLowerCase().contains("payment"));
    }

    @Test
    @DisplayName("Test Stock Failure Choreography Saga Tracing")
    void testStockFailureChoreographySaga() {
        Order order = orderService.createOrder("CUST-002", "OUT_OF_STOCK", 200.0);
        assertNotNull(order);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertNotNull(order.getFailureReason());
        assertTrue(order.getFailureReason().toLowerCase().contains("out of stock"));
    }
}
