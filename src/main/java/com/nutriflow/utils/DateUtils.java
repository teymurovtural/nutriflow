package com.nutriflow.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date operations.
 * Date calculations, formatting and validation.
 */
@Slf4j
public class DateUtils {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            log.warn("daysBetween: null date value provided");
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculates the number of weeks between two dates.
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return Number of weeks
     */
    public static long weeksBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.WEEKS.between(startDate, endDate);
    }

    /**
     * Calculates the number of months between two dates.
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return Number of months
     */
    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    /**
     * Formats a date to "dd-MM-yyyy" String.
     *
     * @param date Date
     * @return Formatted date string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a DateTime to "dd-MM-yyyy HH:mm:ss" String.
     *
     * @param dateTime DateTime
     * @return Formatted datetime string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Formats time to "HH:mm" String.
     *
     * @param dateTime DateTime
     * @return Formatted time string
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * Parses a String and converts it to LocalDate.
     *
     * @param dateString Date in "dd-MM-yyyy" format
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception e) {
            log.error("Failed to parse date: {}", dateString);
            return null;
        }
    }

    /**
     * Parses a String and converts it to LocalDateTime.
     *
     * @param dateTimeString DateTime in "dd-MM-yyyy HH:mm:ss" format
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        } catch (Exception e) {
            log.error("Failed to parse datetime: {}", dateTimeString);
            return null;
        }
    }

    /**
     * Checks if the given date is before today.
     *
     * @param date Date to compare
     * @return true if the date is before today
     */
    public static boolean isBeforeToday(LocalDate date) {
        if (date == null) return false;
        return date.isBefore(LocalDate.now());
    }

    /**
     * Checks if the given date is after today.
     *
     * @param date Date to compare
     * @return true if the date is after today
     */
    public static boolean isAfterToday(LocalDate date) {
        if (date == null) return false;
        return date.isAfter(LocalDate.now());
    }

    /**
     * Checks if the given date is today.
     *
     * @param date Date to compare
     * @return true if the date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) return false;
        return date.isEqual(LocalDate.now());
    }

    /**
     * Calculates subscription progress.
     *
     * @param startDate      Start date
     * @param endDate        End date
     * @param completedCount Number of completed deliveries
     * @return Progress percentage (0-100)
     */
    public static double calculateSubscriptionProgress(LocalDate startDate, LocalDate endDate, long completedCount) {
        if (startDate == null || endDate == null) {
            return 0.0;
        }

        long totalDays = daysBetween(startDate, endDate);
        if (totalDays <= 0) {
            return 0.0;
        }

        double progress = (completedCount * 100.0) / totalDays;
        return Math.min(Math.round(progress * 10.0) / 10.0, 100.0);
    }

    /**
     * Calculates the remaining days of a subscription.
     *
     * @param endDate End date
     * @return Number of remaining days
     */
    public static long getRemainingDays(LocalDate endDate) {
        if (endDate == null) return 0;

        long remaining = daysBetween(LocalDate.now(), endDate);
        return Math.max(remaining, 0);
    }

    /**
     * Advances a date by a certain number of days.
     *
     * @param date Start date
     * @param days Number of days to add
     * @return New date
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) return null;
        return date.plusDays(days);
    }

    /**
     * Advances a date by a certain number of weeks.
     *
     * @param date  Start date
     * @param weeks Number of weeks to add
     * @return New date
     */
    public static LocalDate addWeeks(LocalDate date, long weeks) {
        if (date == null) return null;
        return date.plusWeeks(weeks);
    }

    /**
     * Advances a date by a certain number of months.
     *
     * @param date   Start date
     * @param months Number of months to add
     * @return New date
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        if (date == null) return null;
        return date.plusMonths(months);
    }

    /**
     * Returns the first day of the given month.
     *
     * @param year  Year
     * @param month Month (1-12)
     * @return First day of the month
     */
    public static LocalDate getFirstDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    /**
     * Returns the last day of the given month.
     *
     * @param year  Year
     * @param month Month (1-12)
     * @return Last day of the month
     */
    public static LocalDate getLastDayOfMonth(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        return firstDay.withDayOfMonth(firstDay.lengthOfMonth());
    }

    /**
     * Checks if a date is between two dates (inclusive).
     *
     * @param date      Date to check
     * @param startDate Start date
     * @param endDate   End date
     * @return true if the date is within the range
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}