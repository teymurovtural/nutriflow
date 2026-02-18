package com.nutriflow.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class EnhancedLoggingAspect {

    // Constants
    private static final String SYMBOL_START = "→";
    private static final String SYMBOL_END = "←";
    private static final String SYMBOL_ERROR = "✗";
    private static final int MAX_LOG_LENGTH = 200;
    private static final long SLOW_METHOD_THRESHOLD = 1000; // 1 second
    private static final long VERY_SLOW_METHOD_THRESHOLD = 5000; // 5 seconds
    private static final String[] SENSITIVE_FIELDS = {
            "password", "token", "secret", "apiKey", "accessToken",
            "refreshToken", "sessionId", "creditCard", "cvv"
    };

    /**
     * Logs all methods in the Service layer
     */
    @Around("execution(* com.nutriflow.services.impl..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "SERVICE");
    }

    /**
     * Logs all methods in the Controller layer
     */
    @Around("execution(* com.nutriflow.controllers..*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "CONTROLLER");
    }

    /**
     * Logs custom methods in the Repository layer
     */
    @Around("@within(org.springframework.stereotype.Repository) && " +
            "!execution(* org.springframework.data.repository..*(..))")
    public Object logRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "REPOSITORY");
    }

    /**
     * Logs method execution logic and monitors performance
     */
    private Object logMethodExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        // Method started
        log.info("{} [{}] {}.{} started | Parameters: {}",
                SYMBOL_START, layer, className, methodName, formatArguments(args));

        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        Throwable exception = null;

        try {
            // Actual method execution
            result = joinPoint.proceed();
            return result;

        } catch (Throwable e) {
            success = false;
            exception = e;

            // Exception log (detailed)
            log.error("{} [{}] {}.{} an error occurred | Exception: {} | Message: {}",
                    SYMBOL_ERROR, layer, className, methodName,
                    e.getClass().getSimpleName(), e.getMessage(), e);

            throw e;

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                // Successful completion
                log.info("{} [{}] {}.{} completed | Duration: {}ms | Return: {}",
                        SYMBOL_END, layer, className, methodName,
                        duration, formatReturnValue(result));
            } else {
                // Failed completion
                log.error("{} [{}] {}.{} failed | Duration: {}ms | Exception: {}",
                        SYMBOL_ERROR, layer, className, methodName,
                        duration, exception.getClass().getSimpleName());
            }

            // Performance warning (slow methods)
            checkPerformance(layer, className, methodName, duration);
        }
    }

    /**
     * Checks performance and detects slow methods
     */
    private void checkPerformance(String layer, String className, String methodName, long duration) {
        if (duration > VERY_SLOW_METHOD_THRESHOLD) {
            log.warn("⚠️ [PERFORMANCE] VERY SLOW METHOD | [{}] {}.{} | Duration: {}ms | Limit: {}ms",
                    layer, className, methodName, duration, VERY_SLOW_METHOD_THRESHOLD);
        } else if (duration > SLOW_METHOD_THRESHOLD) {
            log.warn("⚠️ [PERFORMANCE] Slow method | [{}] {}.{} | Duration: {}ms | Limit: {}ms",
                    layer, className, methodName, duration, SLOW_METHOD_THRESHOLD);
        }
    }

    /**
     * Formats method arguments and masks sensitive data
     */
    private String formatArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "none";
        }

        Object[] sanitizedArgs = Arrays.stream(args)
                .map(this::sanitizeSensitiveData)
                .toArray();

        return Arrays.toString(sanitizedArgs);
    }

    /**
     * Formats return value
     */
    private String formatReturnValue(Object result) {
        if (result == null) {
            return "null";
        }

        String resultStr = sanitizeSensitiveData(result).toString();

        // Truncate very long responses
        if (resultStr.length() > MAX_LOG_LENGTH) {
            return resultStr.substring(0, MAX_LOG_LENGTH) + "... (truncated)";
        }

        return resultStr;
    }

    /**
     * Masks sensitive information
     */
    private Object sanitizeSensitiveData(Object obj) {
        if (obj == null) {
            return null;
        }

        String objStr = obj.toString();

        // Mask sensitive fields
        for (String field : SENSITIVE_FIELDS) {
            objStr = objStr.replaceAll(field + "=[^,\\]\\)\\s]+", field + "=***");
        }

        // Mask credit card numbers
        objStr = objStr.replaceAll("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b",
                "****-****-****-****");

        // Partially mask emails (keep first 3 characters)
        objStr = objStr.replaceAll("([a-zA-Z0-9]{3})[a-zA-Z0-9._-]+@", "$1***@");

        return objStr;
    }
}
