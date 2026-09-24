package com.rikkei.b2.messaging;

import com.rikkei.b2.model.OrderEvent;
import com.rikkei.b2.tracing.MdcTraceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Simulates Apache Kafka Topic message bus with Header Propagation (X-Correlation-ID).
 * Ensures Choreography Saga events carry Correlation ID in headers and MDC tracing.
 */
@Component
public class KafkaEventBus {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventBus.class);
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    private final Map<String, List<BiConsumer<OrderEvent, Map<String, String>>>> topicListeners = new ConcurrentHashMap<>();

    public void subscribe(String topic, BiConsumer<OrderEvent, Map<String, String>> listener) {
        topicListeners.computeIfAbsent(topic, k -> new ArrayList<>()).add(listener);
    }

    public void publish(String topic, OrderEvent event, Map<String, String> headers) {
        Map<String, String> kafkaHeaders = new HashMap<>(headers != null ? headers : Map.of());
        if (event.getCorrelationId() != null) {
            kafkaHeaders.put(HEADER_CORRELATION_ID, event.getCorrelationId());
        }

        String correlationId = kafkaHeaders.get(HEADER_CORRELATION_ID);
        log.info("[KAFKA TOPIC: {}] Publishing Event: {} | Headers: {}", topic, event.getEventType(), kafkaHeaders);

        List<BiConsumer<OrderEvent, Map<String, String>>> listeners = topicListeners.get(topic);
        if (listeners != null) {
            for (BiConsumer<OrderEvent, Map<String, String>> listener : listeners) {
                // Propagate Correlation ID to MDC context for receiver thread tracing
                MdcTraceUtil.setCorrelationId(correlationId);
                try {
                    listener.accept(event, kafkaHeaders);
                } finally {
                    MdcTraceUtil.clear();
                }
            }
        }
    }
}
