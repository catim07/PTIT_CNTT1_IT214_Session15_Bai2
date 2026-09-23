package com.rikkei.b2.orchestrator;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderSagaOrchestrator {
    private final Map<String, String> sagaState = new ConcurrentHashMap<>();

    public boolean executeSaga(String orderId, double amount, int quantity) {
        sagaState.put(orderId, "STARTED");

        boolean paymentOk = processPayment(orderId, amount);
        if (!paymentOk) {
            sagaState.put(orderId, "FAILED_PAYMENT");
            return false;
        }

        boolean stockOk = reserveStock(orderId, quantity);
        if (!stockOk) {
            compensatePayment(orderId, amount);
            sagaState.put(orderId, "FAILED_STOCK_COMPENSATED");
            return false;
        }

        sagaState.put(orderId, "SUCCESS");
        return true;
    }

    private boolean processPayment(String orderId, double amount) {
        return amount > 0 && amount <= 1000;
    }

    private boolean reserveStock(String orderId, int quantity) {
        return quantity > 0 && quantity <= 50;
    }

    private void compensatePayment(String orderId, double amount) {
        // Refund logic
    }

    public String getSagaState(String orderId) {
        return sagaState.getOrDefault(orderId, "NOT_FOUND");
    }
}
