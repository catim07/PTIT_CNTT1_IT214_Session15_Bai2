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
public class StockService {
    private static final Logger log = LoggerFactory.getLogger(StockService.class);
    public static final String TOPIC_STOCK_EVENTS = "stock-events";

    private final KafkaEventBus eventBus;

    public StockService(KafkaEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void initSubscribers() {
        // Listen to payment-events for PAYMENT_PROCESSED
        eventBus.subscribe("payment-events", (event, headers) -> {
            String correlationId = headers.get(KafkaEventBus.HEADER_CORRELATION_ID);
            MdcTraceUtil.setCorrelationId(correlationId);
            try {
                if (event.getEventType() == OrderEventType.PAYMENT_PROCESSED) {
                    reserveStock(event, correlationId);
                }
            } finally {
                MdcTraceUtil.clear();
            }
        });
    }

    public void reserveStock(OrderEvent event, String correlationId) {
        log.info("[StockService] Reserving stock for Order: {} | Item: {}", event.getOrderId(), event.getItem());

        boolean stockAvailable = !"OUT_OF_STOCK".equalsIgnoreCase(event.getItem()) && !"FAIL_STOCK".equalsIgnoreCase(event.getItem());

        OrderEvent responseEvent;
        if (stockAvailable) {
            log.info("[StockService] Stock RESERVED for Order: {}", event.getOrderId());
            responseEvent = new OrderEvent(
                    UUID.randomUUID().toString(),
                    OrderEventType.STOCK_RESERVED,
                    event.getOrderId(), event.getCustomerId(), event.getItem(), event.getAmount(), correlationId, null
            );
        } else {
            String reason = "Item '" + event.getItem() + "' is out of stock";
            log.warn("[StockService] Stock RESERVATION FAILED for Order: {} | Reason: {}", event.getOrderId(), reason);
            responseEvent = new OrderEvent(
                    UUID.randomUUID().toString(),
                    OrderEventType.STOCK_FAILED,
                    event.getOrderId(), event.getCustomerId(), event.getItem(), event.getAmount(), correlationId, reason
            );
        }

        Map<String, String> headers = Map.of(KafkaEventBus.HEADER_CORRELATION_ID, correlationId);
        eventBus.publish(TOPIC_STOCK_EVENTS, responseEvent, headers);
    }
}
