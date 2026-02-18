package com.nutriflow.constants;

/**
 * Constant values for logging
 *
 * Used by LoggingAspect and other logging classes
 */
public final class LoggingConstants {

    private LoggingConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Layer identifiers
    public static final String LAYER_SERVICE = "SERVICE";
    public static final String LAYER_CONTROLLER = "CONTROLLER";
    public static final String LAYER_REPOSITORY = "REPOSITORY";

    // Log symbols
    public static final String SYMBOL_START = "→";
    public static final String SYMBOL_END = "←";
    public static final String SYMBOL_ERROR = "✗";
    public static final String SYMBOL_SUCCESS = "✓";

    // Log templates
    public static final String TEMPLATE_METHOD_START = "{} [{}] {}.{} started | Parameters: {}";
    public static final String TEMPLATE_METHOD_END = "{} [{}] {}.{} completed | Duration: {}ms | Return: {}";
    public static final String TEMPLATE_METHOD_ERROR = "{} [{}] {}.{} error occurred | Exception: {} | Message: {}";
    public static final String TEMPLATE_METHOD_FAILED = "{} [{}] {}.{} failed | Duration: {}ms";

    // Sensitive fields to hide
    public static final String[] SENSITIVE_FIELDS = {
            "password", "token", "secret", "apiKey", "accessToken",
            "refreshToken", "sessionId", "creditCard", "cvv"
    };

    // Max length for log output
    public static final int MAX_LOG_LENGTH = 200;

    // Time thresholds (ms)
    public static final long SLOW_METHOD_THRESHOLD = 1000; // 1 second
    public static final long VERY_SLOW_METHOD_THRESHOLD = 5000; // 5 seconds
}