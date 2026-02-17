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
 * Delivery əməliyyatları üçün helper sinif.
 * Delivery yaratma, filtrasiya və business logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryHelper {

    private final DeliveryRepository deliveryRepository;

    /**
     * MenuBatch təsdiq edildikdən sonra hər gün üçün Delivery yaradır.
     *
     * @param batch           MenuBatch
     * @param user            User
     * @param deliveryNotes   Çatdırılma qeydləri
     */
    @Transactional
    public void createDeliveriesForApprovedBatch(MenuBatchEntity batch, UserEntity user, String deliveryNotes) {
        log.info("Təsdiqlənmiş batch üçün delivery-lər yaradılır. BatchId: {}, UserId: {}", batch.getId(), user.getId());

        List<Integer> distinctDays = batch.getItems().stream()
                .map(MenuItemEntity::getDay)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Batch-ə aid mövcud delivery-ləri bir dəfə tap
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

                // Mövcud delivery-ni tap
                DeliveryEntity existingDelivery = existingDeliveries.stream()
                        .filter(d -> d.getDate().equals(deliveryDate))
                        .findFirst()
                        .orElse(null);

                if (existingDelivery != null) {
                    // Mövcuddursa - yalnız notes yenilə
                    if (deliveryNotes != null && !deliveryNotes.isBlank()) {
                        existingDelivery.setDeliveryNotes(deliveryNotes);
                        deliveryRepository.save(existingDelivery);
                        updatedCount++;
                        log.debug("Delivery notes yeniləndi: Date={}", deliveryDate);
                    }
                    continue;
                }

                // Yoxdursa - yeni yarat
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

                log.debug("Delivery yaradıldı: Date={}, Status={}", deliveryDate, DeliveryStatus.PENDING);

            } catch (Exception e) {
                log.error("Gün {} üçün delivery yaradılarkən xəta: {}", day, e.getMessage());
            }
        }

        log.info("Batch üçün {} delivery yaradıldı, {} delivery yeniləndi", createdCount, updatedCount);
    }

    /**
     * User-ə aid olan bütün delivery-ləri statusuna görə filtrlər.
     *
     * @param userId User ID
     * @param status DeliveryStatus (null olarsa hamısı)
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
     * Caterer-ə aid olan müəyyən tarixdəki delivery-ləri filtrlər.
     *
     * @param catererId Caterer ID
     * @param date      Tarix (null olarsa bugün)
     * @param name      Müştəri adı (optional)
     * @param district  Rayon (optional)
     * @return Filtered deliveries
     */
    public List<DeliveryEntity> getDeliveriesByCatererAndDate(Long catererId, LocalDate date, String name, String district) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        return deliveryRepository.searchDeliveries(catererId, searchDate, name, district);
    }

    /**
     * Müəyyən günün menu item-lərini çıxarır.
     *
     * @param batch MenuBatch
     * @param day   Gün
     * @return Həmin günün menu items
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
     * Delivery-nin statusunu yeniləyir və əlavə məntiq tətbiq edir.
     *
     * @param delivery  Delivery entity
     * @param newStatus Yeni status
     * @param note      Caterer qeydi
     */
    @Transactional
    public void updateDeliveryStatus(DeliveryEntity delivery, DeliveryStatus newStatus, String note) {
        log.info("Delivery status yenilənir: ID={}, OldStatus={}, NewStatus={}",
                delivery.getId(), delivery.getStatus(), newStatus);

        // DELIVERED-dən başqa statusa keçid olmaz
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Çatdırılmış sifariş artıq dəyişdirilə bilməz!");
        }

        // FAILED-dən yalnız PENDING-ə qayıda bilər (retry)
        if (delivery.getStatus() == DeliveryStatus.FAILED
                && newStatus != DeliveryStatus.PENDING) {
            throw new IllegalStateException("Uğursuz sifariş yalnız yenidən PENDING-ə qaytarıla bilər!");
        }

        delivery.setStatus(newStatus);

        if (note != null && !note.isBlank()) {
            delivery.setCatererNote(note);
        }

        if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setActualDeliveryTime(java.time.LocalDateTime.now());
            log.info("Delivery tamamlandı: ID={}, Time={}", delivery.getId(), delivery.getActualDeliveryTime());
        }

        if (newStatus == DeliveryStatus.FAILED) {
            delivery.setActualDeliveryTime(java.time.LocalDateTime.now());
            log.warn("Delivery uğursuz oldu: ID={}, Reason={}", delivery.getId(), note);
        }

        // PENDING-ə retry zamanı vaxtı sıfırla
        if (newStatus == DeliveryStatus.PENDING) {
            delivery.setActualDeliveryTime(null);
            delivery.setEstimatedDeliveryTime(null);
            log.info("Delivery retry üçün sıfırlandı: ID={}", delivery.getId());
        }

        deliveryRepository.save(delivery);
    }

    /**
     * Estimated delivery time yeniləyir.
     *
     * @param delivery      Delivery entity
     * @param estimatedTime Təxmini vaxt
     */
    @Transactional
    public void updateEstimatedTime(DeliveryEntity delivery, String estimatedTime) {
        // Əgər artıq çatdırılıbsa, vaxtı dəyişdirmək olmaz
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Sifariş artıq çatdırılıb! Təxmini vaxtı dəyişdirə bilməzsiniz.");
        }

        delivery.setEstimatedDeliveryTime(estimatedTime);
        deliveryRepository.save(delivery);

        log.info("Estimated time yeniləndi: DeliveryId={}, Time={}", delivery.getId(), estimatedTime);
    }

    /**
     * Keçmişdəki delivery-ləri tapır.
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
     * Gələcəkdəki delivery-ləri tapır.
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
     * Bugünkü delivery-ləri tapır.
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
     * Caterer statistikası hesablayır.
     *
     * @param catererId Caterer ID
     * @param date      Tarix
     * @return Status sayları
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