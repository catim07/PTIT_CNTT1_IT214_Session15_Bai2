package com.rikkei.b2.controller;

import com.rikkei.b2.model.Order;
import com.rikkei.b2.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> request) {
        String customerId = (String) request.getOrDefault("customerId", "CUST-001");
        String item = (String) request.getOrDefault("item", "Laptop Dell XPS");
        double amount = Double.parseDouble(request.getOrDefault("amount", 1200.0).toString());

        Order order = orderService.createOrder(customerId, item, amount);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<Collection<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders().values());
    }
}
