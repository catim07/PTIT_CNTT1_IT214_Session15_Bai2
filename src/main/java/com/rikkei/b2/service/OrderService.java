package com.rikkei.b2.service;

import com.rikkei.b2.messaging.KafkaEventBus;
import com.rikkei.b2.model.Order;
import com.rikkei.b2.model.OrderEvent;
import com.rikkei.b2.model.OrderEventType;
import com.rikkei.b2.model.OrderStatus;
import com.rikkei.b2.tracing.MdcTraceUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    public static final String TOPIC_ORDER_EVENTS = "order-events";

    private final KafkaEventBus eventBus;
    private final Map<String, Order> orderRepository = new ConcurrentHashMap<>();

    public OrderService(KafkaEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void initSubscribers() {
        // Choreography listener for Payment events
        eventBus.subscribe("payment-events", (event, headers) -> {
            String correlationId = headers.get(KafkaEventBus.HEADER_CORRELATION_ID);
            MdcTraceUtil.setCorrelationId(correlationId);
            try {
                if (event.getEventType() == OrderEventType.PAYMENT_FAILED) {
                    handlePaymentFailed(event);
                }
            } finally {
                MdcTraceUtil.clear();
            }
        });

        // Choreography listener for Stock events
        eventBus.subscribe("stock-events", (event, headers) -> {
            String correlationId = headers.get(KafkaEventBus.HEADER_CORRELATION_ID);
            MdcTraceUtil.setCorrelationId(correlationId);
            try {
                if (event.getEventType() == OrderEventType.STOCK_RESERVED) {
                    handleStockReserved(event);
                } else if (event.getEventType() == OrderEventType.STOCK_FAILED) {
                    handleStockFailed(event);
                }
            } finally {
                MdcTraceUtil.clear();
            }
        });
    }

    public Order createOrder(String customerId, String item, double amount) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        String correlationId = "TRACE-" + UUID.randomUUID().toString().substring(0, 8);

        MdcTraceUtil.setCorrelationId(correlationId);
        try {
            Order order = new Order(orderId, customerId, item, amount, OrderStatus.PENDING);
            orderRepository.put(orderId, order);

            log.info("[OrderService] Created Order: {} for Customer: {} | Amount: ${}", orderId, customerId, amount);

            OrderEvent event = new OrderEvent(
                    UUID.randomUUID().toString(),
                    OrderEventType.ORDER_CREATED,
                    orderId, customerId, item, amount, correlationId, null
            );

            Map<String, String> headers = Map.of(KafkaEventBus.HEADER_CORRELATION_ID, correlationId);
            eventBus.publish(TOPIC_ORDER_EVENTS, event, headers);

            return order;
        } finally {
            MdcTraceUtil.clear();
        }
    }

    private void handlePaymentFailed(OrderEvent event) {
        Order order = orderRepository.get(event.getOrderId());
        if (order != null) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setFailureReason(event.getReason());
            log.error("[OrderService] Order {} CANCELLED due to Payment Failure: {}", event.getOrderId(), event.getReason());
        }
    }

    private void handleStockFailed(OrderEvent event) {
        Order order = orderRepository.get(event.getOrderId());
        if (order != null) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setFailureReason(event.getReason());
            log.error("[OrderService] Order {} CANCELLED due to Stock Failure: {}", event.getOrderId(), event.getReason());
        }
    }

    private void handleStockReserved(OrderEvent event) {
        Order order = orderRepository.get(event.getOrderId());
        if (order != null) {
            order.setStatus(OrderStatus.COMPLETED);
            log.info("[OrderService] Order {} SUCCESSFUL and marked COMPLETED!", event.getOrderId());
        }
    }

    public Order getOrder(String orderId) {
        return orderRepository.get(orderId);
    }

    public Map<String, Order> getAllOrders() {
        return orderRepository;
    }
}
