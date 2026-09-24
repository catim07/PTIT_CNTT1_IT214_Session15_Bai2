package com.rikkei.b2.service;

import com.rikkei.b2.messaging.KafkaEventBus;
import com.rikkei.b2.model.OrderEvent;
import com.rikkei.b2.model.OrderEventType;
import com.rikkei.b2.tracing.MdcTraceUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    public static final String TOPIC_PAYMENT_EVENTS = "payment-events";

    private final KafkaEventBus eventBus;

    public PaymentService(KafkaEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void initSubscribers() {
        // Listen to order-events for ORDER_CREATED
        eventBus.subscribe("order-events", (event, headers) -> {
            String correlationId = headers.get(KafkaEventBus.HEADER_CORRELATION_ID);
            MdcTraceUtil.setCorrelationId(correlationId);
            try {
                if (event.getEventType() == OrderEventType.ORDER_CREATED) {
                    processPayment(event, correlationId);
                }
            } finally {
                MdcTraceUtil.clear();
            }
        });
    }

    public void processPayment(OrderEvent event, String correlationId) {
        log.info("[PaymentService] Processing payment for Order: {} | Amount: ${}", event.getOrderId(), event.getAmount());

        boolean paymentSuccess = event.getAmount() <= 5000 && !("FAIL_PAYMENT".equalsIgnoreCase(event.getCustomerId()));

        OrderEvent responseEvent;
        if (paymentSuccess) {
            log.info("[PaymentService] Payment SUCCESSFUL for Order: {}", event.getOrderId());
            responseEvent = new OrderEvent(
                    UUID.randomUUID().toString(),
                    OrderEventType.PAYMENT_PROCESSED,
                    event.getOrderId(), event.getCustomerId(), event.getItem(), event.getAmount(), correlationId, null
            );
        } else {
            String reason = event.getAmount() > 5000 ? "Exceeded payment limit ($5000)" : "Simulated payment rejection";
            log.warn("[PaymentService] Payment REJECTED for Order: {} | Reason: {}", event.getOrderId(), reason);
            responseEvent = new OrderEvent(
                    UUID.randomUUID().toString(),
                    OrderEventType.PAYMENT_FAILED,
                    event.getOrderId(), event.getCustomerId(), event.getItem(), event.getAmount(), correlationId, reason
            );
        }

        Map<String, String> headers = Map.of(KafkaEventBus.HEADER_CORRELATION_ID, correlationId);
        eventBus.publish(TOPIC_PAYMENT_EVENTS, responseEvent, headers);
    }
}
