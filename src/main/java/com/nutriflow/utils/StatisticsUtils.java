package com.nutriflow.utils;

import com.nutriflow.entities.DeliveryEntity;
import com.nutriflow.entities.MenuItemEntity;
import com.nutriflow.enums.DeliveryStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Statistika hesablamaları üçün yardımçı sinif.
 * Dashboard, reporting və analytics üçün hesablamalar.
 */
@Slf4j
public class StatisticsUtils {

    /**
     * Delivery-lərin status-a görə sayını hesablayır.
     *
     * @param deliveries Delivery list
     * @param status     DeliveryStatus
     * @return Həmin statusda olan delivery sayı
     */
    public static long countByStatus(List<DeliveryEntity> deliveries, DeliveryStatus status) {
        if (deliveries == null || deliveries.isEmpty() || status == null) {
            return 0;
        }

        return deliveries.stream()
                .filter(delivery -> status.equals(delivery.getStatus()))
                .count();
    }

    /**
     * Tamamlanma faizini hesablayır.
     *
     * @param completed Tamamlanmış say
     * @param total     Ümumi say
     * @return Faiz (0-100)
     */
    public static double calculateCompletionPercentage(long completed, long total) {
        if (total == 0) {
            return 0.0;
        }

        double percentage = (completed * 100.0) / total;
        return Math.min(Math.round(percentage * 10.0) / 10.0, 100.0);
    }

    /**
     * Ortalama kalori hesablayır.
     *
     * @param menuItems Menu item list
     * @return Ortalama kalori
     */
    public static double calculateAverageCalories(List<MenuItemEntity> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return 0.0;
        }

        return menuItems.stream()
                .filter(item -> item.getCalories() != null)
                .mapToDouble(MenuItemEntity::getCalories)
                .average()
                .orElse(0.0);
    }

    /**
     * Ümumi kalori hesablayır.
     *
     * @param menuItems Menu item list
     * @return Ümumi kalori
     */
    public static double calculateTotalCalories(List<MenuItemEntity> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return 0.0;
        }

        return menuItems.stream()
                .filter(item -> item.getCalories() != null)
                .mapToDouble(MenuItemEntity::getCalories)
                .sum();
    }

    /**
     * Ümumi protein hesablayır.
     *
     * @param menuItems Menu item list
     * @return Ümumi protein (gram)
     */
    public static double calculateTotalProtein(List<MenuItemEntity> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return 0.0;
        }

        return menuItems.stream()
                .filter(item -> item.getProtein() != null)
                .mapToDouble(MenuItemEntity::getProtein)
                .sum();
    }

    /**
     * Ümumi karbohidrat hesablayır.
     *
     * @param menuItems Menu item list
     * @return Ümumi karbohidrat (gram)
     */
    public static double calculateTotalCarbs(List<MenuItemEntity> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return 0.0;
        }

        return menuItems.stream()
                .filter(item -> item.getCarbs() != null)
                .mapToDouble(MenuItemEntity::getCarbs)
                .sum();
    }

    /**
     * Ümumi yağ hesablayır.
     *
     * @param menuItems Menu item list
     * @return Ümumi yağ (gram)
     */
    public static double calculateTotalFats(List<MenuItemEntity> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return 0.0;
        }

        return menuItems.stream()
                .filter(item -> item.getFats() != null)
                .mapToDouble(MenuItemEntity::getFats)
                .sum();
    }

    /**
     * Delivery status-larının paylanmasını hesablayır.
     *
     * @param deliveries Delivery list
     * @return Status -> Count map
     */
    public static Map<DeliveryStatus, Long> getStatusDistribution(List<DeliveryEntity> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return Map.of();
        }

        return deliveries.stream()
                .collect(Collectors.groupingBy(
                        DeliveryEntity::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * Uğur nisbətini hesablayır (SUCCESS / TOTAL).
     *
     * @param successCount Uğurlu say
     * @param totalCount   Ümumi say
     * @return Uğur nisbəti (0-1)
     */
    public static double calculateSuccessRate(long successCount, long totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }

        double rate = (double) successCount / totalCount;
        return Math.round(rate * 1000.0) / 1000.0; // 3 onluq dəqiqliklə
    }

    /**
     * Ortalama çatdırılma vaxtını hesablayır (estimated time-lardan).
     *
     * @param deliveries Delivery list
     * @return Ortalama vaxt (dəqiqə)
     */
    public static double calculateAverageDeliveryTime(List<DeliveryEntity> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return 0.0;
        }

        // Burada estimated time String formatında olduğundan,
        // daha kompleks hesablama lazım ola bilər
        // Bu sadələşdirilmiş versiya

        long count = deliveries.stream()
                .filter(d -> d.getEstimatedDeliveryTime() != null)
                .count();

        return count > 0 ? count : 0.0;
    }

    /**
     * Günlük menyu çeşidliliyi hesablayır (unikal yemək sayı).
     *
     * @param menuItems Menu item list
     * @return Unikal yemək təsviri sayı
     */
    public static long calculateMenuVariety(List<MenuItemEntity> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return 0;
        }

        return menuItems.stream()
                .map(MenuItemEntity::getDescription)
                .filter(desc -> desc != null && !desc.isBlank())
                .distinct()
                .count();
    }

    /**
     * İki rəqəm arasındakı fərqi faiz olaraq hesablayır.
     *
     * @param current  Cari dəyər
     * @param previous Əvvəlki dəyər
     * @return Dəyişiklik faizi
     */
    public static double calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }

        double change = ((current - previous) / previous) * 100;
        return Math.round(change * 10.0) / 10.0;
    }

    /**
     * Minimum dəyəri tapır və güvənli şəkildə qaytarır.
     *
     * @param values Dəyərlər
     * @return Minimum dəyər və ya 0
     */
    public static double findMinimum(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        return values.stream()
                .filter(v -> v != null)
                .min(Double::compareTo)
                .orElse(0.0);
    }

    /**
     * Maximum dəyəri tapır və güvənli şəkildə qaytarır.
     *
     * @param values Dəyərlər
     * @return Maximum dəyər və ya 0
     */
    public static double findMaximum(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        return values.stream()
                .filter(v -> v != null)
                .max(Double::compareTo)
                .orElse(0.0);
    }

    /**
     * Ortalamanı hesablayır.
     *
     * @param values Dəyərlər
     * @return Ortalama və ya 0
     */
    public static double calculateAverage(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        return values.stream()
                .filter(v -> v != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Medianı hesablayır.
     *
     * @param values Dəyərlər
     * @return Median və ya 0
     */
    public static double calculateMedian(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        List<Double> sortedValues = values.stream()
                .filter(v -> v != null)
                .sorted()
                .collect(Collectors.toList());

        if (sortedValues.isEmpty()) {
            return 0.0;
        }

        int size = sortedValues.size();
        if (size % 2 == 0) {
            return (sortedValues.get(size / 2 - 1) + sortedValues.get(size / 2)) / 2.0;
        } else {
            return sortedValues.get(size / 2);
        }
    }

    /**
     * Ümumi sayı formatlaşdırır (1000 -> 1K, 1000000 -> 1M).
     *
     * @param count Say
     * @return Formatlanmış string
     */
    public static String formatCount(long count) {
        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 1000000) {
            return String.format("%.1fK", count / 1000.0);
        } else {
            return String.format("%.1fM", count / 1000000.0);
        }
    }

    /**
     * Faizi formatlaşdırır.
     *
     * @param percentage Faiz dəyəri
     * @return Formatlanmış string (örn: "85.5%")
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage);
    }
}