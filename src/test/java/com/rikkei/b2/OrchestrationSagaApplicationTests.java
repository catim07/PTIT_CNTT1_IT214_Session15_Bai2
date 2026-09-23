package com.rikkei.b2;

import com.rikkei.b2.orchestrator.OrderSagaOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrchestrationSagaApplicationTests {

    @Autowired
    private OrderSagaOrchestrator orchestrator;

    @Test
    void testSuccessfulSaga() {
        boolean success = orchestrator.executeSaga("O201", 150.0, 5);
        assertTrue(success);
        assertEquals("SUCCESS", orchestrator.getSagaState("O201"));
    }

    @Test
    void testFailedStockSagaWithCompensation() {
        boolean success = orchestrator.executeSaga("O202", 150.0, 999);
        assertFalse(success);
        assertEquals("FAILED_STOCK_COMPENSATED", orchestrator.getSagaState("O202"));
    }
}
