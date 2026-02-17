package com.nutriflow.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Tarix əməliyyatları üçün yardımçı sinif.
 * Date calculations, formatting və validation.
 */
@Slf4j
public class DateUtils {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            log.warn("daysBetween: null tarix dəyəri göndərilib");
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * İki tarix arasındaki həftələrin sayını hesablayır.
     *
     * @param startDate Başlanğıc tarixi
     * @param endDate   Son tarix
     * @return Həftələrin sayı
     */
    public static long weeksBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.WEEKS.between(startDate, endDate);
    }

    /**
     * İki tarix arasındaki ayların sayını hesablayır.
     *
     * @param startDate Başlanğıc tarixi
     * @param endDate   Son tarix
     * @return Ayların sayı
     */
    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    /**
     * Tarixi "dd-MM-yyyy" formatında String-ə çevirir.
     *
     * @param date Tarix
     * @return Formatlanmış tarix string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FORMATTER);
    }

    /**
     * DateTime-ı "dd-MM-yyyy HH:mm:ss" formatında String-ə çevirir.
     *
     * @param dateTime DateTime
     * @return Formatlanmış datetime string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Vaxtı "HH:mm" formatında String-ə çevirir.
     *
     * @param dateTime DateTime
     * @return Formatlanmış vaxt string
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * Tarixi parse edib LocalDate-ə çevirir.
     *
     * @param dateString "dd-MM-yyyy" formatında tarix
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception e) {
            log.error("Tarix parse edilə bilmədi: {}", dateString);
            return null;
        }
    }

    /**
     * Tarixi parse edib LocalDateTime-ə çevirir.
     *
     * @param dateTimeString "dd-MM-yyyy HH:mm:ss" formatında datetime
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        } catch (Exception e) {
            log.error("DateTime parse edilə bilmədi: {}", dateTimeString);
            return null;
        }
    }

    /**
     * Verilən tarixi bugünkü tarixlə müqayisə edir.
     *
     * @param date Müqayisə ediləcək tarix
     * @return true əgər tarix bugündən əvvəldirsə
     */
    public static boolean isBeforeToday(LocalDate date) {
        if (date == null) return false;
        return date.isBefore(LocalDate.now());
    }

    /**
     * Verilən tarixi bugünkü tarixlə müqayisə edir.
     *
     * @param date Müqayisə ediləcək tarix
     * @return true əgər tarix bugündən sonradırsa
     */
    public static boolean isAfterToday(LocalDate date) {
        if (date == null) return false;
        return date.isAfter(LocalDate.now());
    }

    /**
     * Tarixi bugünkü tarixlə müqayisə edir.
     *
     * @param date Müqayisə ediləcək tarix
     * @return true əgər tarix bugündürsə
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) return false;
        return date.isEqual(LocalDate.now());
    }

    /**
     * Abunəlik progress hesablayır.
     *
     * @param startDate      Başlanğıc tarixi
     * @param endDate        Bitmə tarixi
     * @param completedCount Tamamlanmış çatdırılma sayı
     * @return Progress faizi (0-100)
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
        return Math.min(Math.round(progress * 10.0) / 10.0, 100.0); // Max 100%
    }

    /**
     * Abunəliyin qalan günlərini hesablayır.
     *
     * @param endDate Bitmə tarixi
     * @return Qalan günlərin sayı
     */
    public static long getRemainingDays(LocalDate endDate) {
        if (endDate == null) return 0;

        long remaining = daysBetween(LocalDate.now(), endDate);
        return Math.max(remaining, 0); // Neqativ olmaz
    }

    /**
     * Tarixi müəyyən gün sonrasına aparır.
     *
     * @param date Başlanğıc tarixi
     * @param days Əlavə ediləcək günlərin sayı
     * @return Yeni tarix
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) return null;
        return date.plusDays(days);
    }

    /**
     * Tarixi müəyyən həftə sonrasına aparır.
     *
     * @param date  Başlanğıc tarixi
     * @param weeks Əlavə ediləcək həftələrin sayı
     * @return Yeni tarix
     */
    public static LocalDate addWeeks(LocalDate date, long weeks) {
        if (date == null) return null;
        return date.plusWeeks(weeks);
    }

    /**
     * Tarixi müəyyən ay sonrasına aparır.
     *
     * @param date   Başlanğıc tarixi
     * @param months Əlavə ediləcək ayların sayı
     * @return Yeni tarix
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        if (date == null) return null;
        return date.plusMonths(months);
    }

    /**
     * Ayın ilk gününü qaytarır.
     *
     * @param year  İl
     * @param month Ay (1-12)
     * @return Ayın ilk günü
     */
    public static LocalDate getFirstDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    /**
     * Ayın son gününü qaytarır.
     *
     * @param year  İl
     * @param month Ay (1-12)
     * @return Ayın son günü
     */
    public static LocalDate getLastDayOfMonth(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        return firstDay.withDayOfMonth(firstDay.lengthOfMonth());
    }

    /**
     * İki tarix arasında olub-olmadığını yoxlayır (inclusive).
     *
     * @param date      Yoxlanılacaq tarix
     * @param startDate Başlanğıc tarixi
     * @param endDate   Son tarix
     * @return true əgər tarix aralıqdadırsa
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}