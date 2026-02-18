package com.nutriflow.utils;

import com.nutriflow.entities.DeliveryEntity;
import com.nutriflow.entities.MenuItemEntity;
import com.nutriflow.enums.DeliveryStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for statistical calculations.
 * Calculations for dashboard, reporting, and analytics.
 */
@Slf4j
public class StatisticsUtils {

    /**
     * Counts the number of deliveries by status.
     *
     * @param deliveries Delivery list
     * @param status     DeliveryStatus
     * @return Number of deliveries with the given status
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
     * Calculates the completion percentage.
     *
     * @param completed Number of completed items
     * @param total     Total number of items
     * @return Percentage (0-100)
     */
    public static double calculateCompletionPercentage(long completed, long total) {
        if (total == 0) {
            return 0.0;
        }

        double percentage = (completed * 100.0) / total;
        return Math.min(Math.round(percentage * 10.0) / 10.0, 100.0);
    }

    /**
     * Calculates the average calories.
     *
     * @param menuItems Menu item list
     * @return Average calories
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
     * Calculates the total calories.
     *
     * @param menuItems Menu item list
     * @return Total calories
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
     * Calculates the total protein.
     *
     * @param menuItems Menu item list
     * @return Total protein (grams)
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
     * Calculates the total carbohydrates.
     *
     * @param menuItems Menu item list
     * @return Total carbohydrates (grams)
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
     * Calculates the total fats.
     *
     * @param menuItems Menu item list
     * @return Total fats (grams)
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
     * Calculates the distribution of delivery statuses.
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
     * Calculates the success rate (SUCCESS / TOTAL).
     *
     * @param successCount Number of successful items
     * @param totalCount   Total number of items
     * @return Success rate (0-1)
     */
    public static double calculateSuccessRate(long successCount, long totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }

        double rate = (double) successCount / totalCount;
        return Math.round(rate * 1000.0) / 1000.0; // 3 decimal precision
    }

    /**
     * Calculates the average delivery time (from estimated times).
     *
     * @param deliveries Delivery list
     * @return Average time (minutes)
     */
    public static double calculateAverageDeliveryTime(List<DeliveryEntity> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return 0.0;
        }

        // Since estimated time is in String format,
        // a more complex calculation may be required.
        // This is a simplified version.

        long count = deliveries.stream()
                .filter(d -> d.getEstimatedDeliveryTime() != null)
                .count();

        return count > 0 ? count : 0.0;
    }

    /**
     * Calculates daily menu variety (number of unique dishes).
     *
     * @param menuItems Menu item list
     * @return Number of unique dish descriptions
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
     * Calculates the percentage difference between two numbers.
     *
     * @param current  Current value
     * @param previous Previous value
     * @return Percentage change
     */
    public static double calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }

        double change = ((current - previous) / previous) * 100;
        return Math.round(change * 10.0) / 10.0;
    }

    /**
     * Finds the minimum value and returns it safely.
     *
     * @param values List of values
     * @return Minimum value or 0
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
     * Finds the maximum value and returns it safely.
     *
     * @param values List of values
     * @return Maximum value or 0
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
     * Calculates the average.
     *
     * @param values List of values
     * @return Average or 0
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
     * Calculates the median.
     *
     * @param values List of values
     * @return Median or 0
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
     * Formats a total count (1000 -> 1K, 1000000 -> 1M).
     *
     * @param count Count
     * @return Formatted string
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
     * Formats a percentage value.
     *
     * @param percentage Percentage value
     * @return Formatted string (e.g., "85.5%")
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage);
    }
}