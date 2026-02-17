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
    private static final long SLOW_METHOD_THRESHOLD = 1000; // 1 saniyə
    private static final long VERY_SLOW_METHOD_THRESHOLD = 5000; // 5 saniyə
    private static final String[] SENSITIVE_FIELDS = {
            "password", "token", "secret", "apiKey", "accessToken",
            "refreshToken", "sessionId", "creditCard", "cvv"
    };

    /**
     * Service layer-dəki bütün metodları log edir
     */
    @Around("execution(* com.nutriflow.services.impl..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "SERVICE");
    }

    /**
     * Controller layer-dəki bütün metodları log edir
     */
    @Around("execution(* com.nutriflow.controllers..*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "CONTROLLER");
    }

    /**
     * Repository layer-dəki custom metodları log edir
     */
    @Around("@within(org.springframework.stereotype.Repository) && " +
            "!execution(* org.springframework.data.repository..*(..))")
    public Object logRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "REPOSITORY");
    }

    /**
     * Method icra məntiqini log edir və performance monitor edir
     */
    private Object logMethodExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        // Method başladı
        log.info("{} [{}] {}.{} başladı | Parametrlər: {}",
                SYMBOL_START, layer, className, methodName, formatArguments(args));

        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        Throwable exception = null;

        try {
            // Actual method icra olunur
            result = joinPoint.proceed();
            return result;

        } catch (Throwable e) {
            success = false;
            exception = e;

            // Exception log (detailed)
            log.error("{} [{}] {}.{} xəta baş verdi | Exception: {} | Mesaj: {}",
                    SYMBOL_ERROR, layer, className, methodName,
                    e.getClass().getSimpleName(), e.getMessage(), e);

            throw e;

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                // Uğurlu completion
                log.info("{} [{}] {}.{} tamamlandı | Müddət: {}ms | Return: {}",
                        SYMBOL_END, layer, className, methodName,
                        duration, formatReturnValue(result));
            } else {
                // Uğursuz completion
                log.error("{} [{}] {}.{} uğursuz oldu | Müddət: {}ms | Exception: {}",
                        SYMBOL_ERROR, layer, className, methodName,
                        duration, exception.getClass().getSimpleName());
            }

            // Performance warning (slow methods)
            checkPerformance(layer, className, methodName, duration);
        }
    }

    /**
     * Performance yoxlayır və slow method-ları detect edir
     */
    private void checkPerformance(String layer, String className, String methodName, long duration) {
        if (duration > VERY_SLOW_METHOD_THRESHOLD) {
            log.warn("⚠️ [PERFORMANCE] ÇOX YAVAŞ METHOD | [{}] {}.{} | Müddət: {}ms | Limit: {}ms",
                    layer, className, methodName, duration, VERY_SLOW_METHOD_THRESHOLD);
        } else if (duration > SLOW_METHOD_THRESHOLD) {
            log.warn("⚠️ [PERFORMANCE] Yavaş method | [{}] {}.{} | Müddət: {}ms | Limit: {}ms",
                    layer, className, methodName, duration, SLOW_METHOD_THRESHOLD);
        }
    }

    /**
     * Method parametrlərini format edir və sensitive data-nı gizlədir
     */
    private String formatArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "yoxdur";
        }

        Object[] sanitizedArgs = Arrays.stream(args)
                .map(this::sanitizeSensitiveData)
                .toArray();

        return Arrays.toString(sanitizedArgs);
    }

    /**
     * Return value-ni format edir
     */
    private String formatReturnValue(Object result) {
        if (result == null) {
            return "null";
        }

        String resultStr = sanitizeSensitiveData(result).toString();

        // Çox uzun response-ları qısalt
        if (resultStr.length() > MAX_LOG_LENGTH) {
            return resultStr.substring(0, MAX_LOG_LENGTH) + "... (truncated)";
        }

        return resultStr;
    }

    /**
     * Sensitive məlumatları gizlədir
     */
    private Object sanitizeSensitiveData(Object obj) {
        if (obj == null) {
            return null;
        }

        String objStr = obj.toString();

        // Sensitive field-ləri gizlət
        for (String field : SENSITIVE_FIELDS) {
            objStr = objStr.replaceAll(field + "=[^,\\]\\)\\s]+", field + "=***");
        }

        // Credit card nömrələrini gizlət
        objStr = objStr.replaceAll("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b",
                "****-****-****-****");

        // Email-ləri partial gizlət (ilk 3 hərf qalır)
        objStr = objStr.replaceAll("([a-zA-Z0-9]{3})[a-zA-Z0-9._-]+@", "$1***@");

        return objStr;
    }
}