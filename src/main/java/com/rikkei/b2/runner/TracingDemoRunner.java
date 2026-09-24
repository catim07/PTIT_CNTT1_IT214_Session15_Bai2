package com.rikkei.b2.runner;

import com.rikkei.b2.model.Order;
import com.rikkei.b2.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TracingDemoRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(TracingDemoRunner.class);

    private final OrderService orderService;

    public TracingDemoRunner(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("==========================================================================");
        log.info("   DEMO: CHOREOGRAPHY SAGA WITH CORRELATION ID & MDC TRACING (SESSION 15)");
        log.info("==========================================================================");

        log.info("\n--- SCENARIO 1: SUCCESSFUL ORDER FLOW ---");
        Order successOrder = orderService.createOrder("CUST-101", "MacBook Pro M3", 2500.0);
        log.info("Final Order Status: {} | OrderId: {}\n", successOrder.getStatus(), successOrder.getOrderId());

        log.info("--- SCENARIO 2: PAYMENT FAILURE FLOW ---");
        Order failedPaymentOrder = orderService.createOrder("FAIL_PAYMENT", "iPhone 15 Pro", 1200.0);
        log.info("Final Order Status: {} | Failure Reason: {}\n", failedPaymentOrder.getStatus(), failedPaymentOrder.getFailureReason());

        log.info("--- SCENARIO 3: STOCK FAILURE FLOW ---");
        Order failedStockOrder = orderService.createOrder("CUST-103", "OUT_OF_STOCK", 300.0);
        log.info("Final Order Status: {} | Failure Reason: {}\n", failedStockOrder.getStatus(), failedStockOrder.getFailureReason());

        log.info("==========================================================================");
    }
}
