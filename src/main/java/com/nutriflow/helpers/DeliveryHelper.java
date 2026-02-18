package com.nutriflow.helpers;

import com.nutriflow.entities.*;
import com.nutriflow.enums.DeliveryStatus;
import com.nutriflow.repositories.DeliveryRepository;
import com.nutriflow.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for Delivery operations.
 * Delivery creation, filtering and business logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryHelper {

    private final DeliveryRepository deliveryRepository;

    /**
     * Creates a Delivery for each day after a MenuBatch is approved.
     *
     * @param batch           MenuBatch
     * @param user            User
     * @param deliveryNotes   Delivery notes
     */
    @Transactional
    public void createDeliveriesForApprovedBatch(MenuBatchEntity batch, UserEntity user, String deliveryNotes) {
        log.info("Creating deliveries for approved batch. BatchId: {}, UserId: {}", batch.getId(), user.getId());

        List<Integer> distinctDays = batch.getItems().stream()
                .map(MenuItemEntity::getDay)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Fetch existing deliveries for the batch once
        List<DeliveryEntity> existingDeliveries = deliveryRepository.findAllByBatchId(batch.getId());

        int createdCount = 0;
        int updatedCount = 0;

        for (Integer day : distinctDays) {
            try {
                LocalDate deliveryDate = LocalDate.of(
                        batch.getMenu().getYear(),
                        batch.getMenu().getMonth(),
                        day
                );

                // Find existing delivery
                DeliveryEntity existingDelivery = existingDeliveries.stream()
                        .filter(d -> d.getDate().equals(deliveryDate))
                        .findFirst()
                        .orElse(null);

                if (existingDelivery != null) {
                    // If exists - only update notes
                    if (deliveryNotes != null && !deliveryNotes.isBlank()) {
                        existingDelivery.setDeliveryNotes(deliveryNotes);
                        deliveryRepository.save(existingDelivery);
                        updatedCount++;
                        log.debug("Delivery notes updated: Date={}", deliveryDate);
                    }
                    continue;
                }

                // If not exists - create new
                DeliveryEntity delivery = DeliveryEntity.builder()
                        .user(user)
                        .caterer(user.getCaterer())
                        .batch(batch)
                        .address(user.getAddress())
                        .date(deliveryDate)
                        .status(DeliveryStatus.PENDING)
                        .deliveryNotes(deliveryNotes)
                        .build();

                deliveryRepository.save(delivery);
                createdCount++;

                log.debug("Delivery created: Date={}, Status={}", deliveryDate, DeliveryStatus.PENDING);

            } catch (Exception e) {
                log.error("Error creating delivery for day {}: {}", day, e.getMessage());
            }
        }

        log.info("{} deliveries created, {} deliveries updated for batch", createdCount, updatedCount);
    }

    /**
     * Filters all deliveries belonging to a user by status.
     *
     * @param userId User ID
     * @param status DeliveryStatus (if null, returns all)
     * @return Filtered deliveries
     */
    public List<DeliveryEntity> getDeliveriesByUserAndStatus(Long userId, DeliveryStatus status) {
        List<DeliveryEntity> allDeliveries = deliveryRepository.findAllByUserId(userId);

        if (status == null) {
            return allDeliveries;
        }

        // Manual filtering
        return allDeliveries.stream()
                .filter(d -> d.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Filters deliveries for a caterer on a specific date.
     *
     * @param catererId Caterer ID
     * @param date      Date (if null, uses today)
     * @param name      Client name (optional)
     * @param district  District (optional)
     * @return Filtered deliveries
     */
    public List<DeliveryEntity> getDeliveriesByCatererAndDate(Long catererId, LocalDate date, String name, String district) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        return deliveryRepository.searchDeliveries(catererId, searchDate, name, district);
    }

    /**
     * Retrieves menu items for a specific day.
     *
     * @param batch MenuBatch
     * @param day   Day
     * @return Menu items for that day
     */
    public List<MenuItemEntity> getMenuItemsForDay(MenuBatchEntity batch, Integer day) {
        if (batch == null || batch.getItems() == null || day == null) {
            return new ArrayList<>();
        }

        return batch.getItems().stream()
                .filter(item -> day.equals(item.getDay()))
                .collect(Collectors.toList());
    }

    /**
     * Updates the delivery status and applies additional business logic.
     *
     * @param delivery  Delivery entity
     * @param newStatus New status
     * @param note      Caterer note
     */
    @Transactional
    public void updateDeliveryStatus(DeliveryEntity delivery, DeliveryStatus newStatus, String note) {
        log.info("Updating delivery status: ID={}, OldStatus={}, NewStatus={}",
                delivery.getId(), delivery.getStatus(), newStatus);

        // Cannot transition from DELIVERED to another status
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("A delivered order can no longer be modified!");
        }

        // From FAILED, can only return to PENDING (retry)
        if (delivery.getStatus() == DeliveryStatus.FAILED
                && newStatus != DeliveryStatus.PENDING) {
            throw new IllegalStateException("A failed order can only be reset back to PENDING!");
        }

        delivery.setStatus(newStatus);

        if (note != null && !note.isBlank()) {
            delivery.setCatererNote(note);
        }

        if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setActualDeliveryTime(java.time.LocalDateTime.now());
            log.info("Delivery completed: ID={}, Time={}", delivery.getId(), delivery.getActualDeliveryTime());
        }

        if (newStatus == DeliveryStatus.FAILED) {
            delivery.setActualDeliveryTime(java.time.LocalDateTime.now());
            log.warn("Delivery failed: ID={}, Reason={}", delivery.getId(), note);
        }

        // Reset time on PENDING retry
        if (newStatus == DeliveryStatus.PENDING) {
            delivery.setActualDeliveryTime(null);
            delivery.setEstimatedDeliveryTime(null);
            log.info("Delivery reset for retry: ID={}", delivery.getId());
        }

        deliveryRepository.save(delivery);
    }

    /**
     * Updates the estimated delivery time.
     *
     * @param delivery      Delivery entity
     * @param estimatedTime Estimated time
     */
    @Transactional
    public void updateEstimatedTime(DeliveryEntity delivery, String estimatedTime) {
        // Cannot change time if already delivered
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Order has already been delivered! You cannot change the estimated time.");
        }

        delivery.setEstimatedDeliveryTime(estimatedTime);
        deliveryRepository.save(delivery);

        log.info("Estimated time updated: DeliveryId={}, Time={}", delivery.getId(), estimatedTime);
    }

    /**
     * Finds past deliveries.
     *
     * @param userId User ID
     * @return Past deliveries
     */
    public List<DeliveryEntity> getPastDeliveries(Long userId) {
        return deliveryRepository.findAllByUserId(userId).stream()
                .filter(delivery -> DateUtils.isBeforeToday(delivery.getDate()))
                .collect(Collectors.toList());
    }

    /**
     * Finds upcoming deliveries.
     *
     * @param userId User ID
     * @return Upcoming deliveries
     */
    public List<DeliveryEntity> getUpcomingDeliveries(Long userId) {
        return deliveryRepository.findAllByUserId(userId).stream()
                .filter(delivery -> !DateUtils.isBeforeToday(delivery.getDate()))
                .collect(Collectors.toList());
    }

    /**
     * Finds today's deliveries.
     *
     * @param userId User ID
     * @return Today's deliveries
     */
    public List<DeliveryEntity> getTodayDeliveries(Long userId) {
        return deliveryRepository.findAllByUserId(userId).stream()
                .filter(delivery -> DateUtils.isToday(delivery.getDate()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates caterer statistics.
     *
     * @param catererId Caterer ID
     * @param date      Date
     * @return Status counts
     */
    public CatererStatsData calculateCatererStats(Long catererId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        return CatererStatsData.builder()
                .totalOrders(deliveryRepository.countByCatererIdAndDate(catererId, targetDate))
                .inProgress(deliveryRepository.countByCatererIdAndDateAndStatus(catererId, targetDate, DeliveryStatus.IN_PROGRESS))
                .ready(deliveryRepository.countByCatererIdAndDateAndStatus(catererId, targetDate, DeliveryStatus.READY))
                .onTheWay(deliveryRepository.countByCatererIdAndDateAndStatus(catererId, targetDate, DeliveryStatus.ON_THE_WAY))
                .delivered(deliveryRepository.countByCatererIdAndDateAndStatus(catererId, targetDate, DeliveryStatus.DELIVERED))
                .failed(deliveryRepository.countByCatererIdAndDateAndStatus(catererId, targetDate, DeliveryStatus.FAILED))
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class CatererStatsData {
        private Long totalOrders;
        private Long inProgress;
        private Long ready;
        private Long onTheWay;
        private Long delivered;
        private Long failed;
    }
}