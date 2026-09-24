package com.rikkei.b2.tracing;

import org.slf4j.MDC;

public class MdcTraceUtil {
    public static final String CORRELATION_ID_KEY = "correlationId";

    public static void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
    }

    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    public static void clear() {
        MDC.remove(CORRELATION_ID_KEY);
    }
}
