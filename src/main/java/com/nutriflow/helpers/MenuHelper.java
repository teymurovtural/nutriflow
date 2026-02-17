package com.nutriflow.helpers;

import com.nutriflow.entities.MenuBatchEntity;
import com.nutriflow.entities.MenuEntity;
import com.nutriflow.entities.MenuItemEntity;
import com.nutriflow.entities.UserEntity;
import com.nutriflow.enums.MenuStatus;
import com.nutriflow.repositories.DeliveryRepository;
import com.nutriflow.repositories.MenuBatchRepository;
import com.nutriflow.repositories.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Menu və MenuBatch əməliyyatları üçün helper sinif.
 * Menu filtering, batch selection və business logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MenuHelper {

    private final MenuRepository menuRepository;
    private final MenuBatchRepository menuBatchRepository;
    private final DeliveryRepository deliveryRepository;

    /**
     * User-in cari ayın menyusunu tapır.
     *
     * @param userId User ID
     * @return MenuEntity (optional)
     */
    public Optional<MenuEntity> getCurrentMonthMenu(Long userId) {
        LocalDate now = LocalDate.now();
        return menuRepository.findByUserIdAndYearAndMonth(userId, now.getYear(), now.getMonthValue());
    }

    /**
     * User-in müəyyən ay və ilin menyusunu tapır.
     *
     * @param userId User ID
     * @param year   İl
     * @param month  Ay
     * @return MenuEntity (optional)
     */
    public Optional<MenuEntity> getMenuByYearAndMonth(Long userId, int year, int month) {
        return menuRepository.findByUserIdAndYearAndMonth(userId, year, month);
    }

    /**
     * MenuEntity-dən aktiv batch-i (APPROVED və ya SUBMITTED) tapır.
     *
     * @param menu MenuEntity
     * @return Aktiv batch (optional)
     */
    public Optional<MenuBatchEntity> getActiveBatch(MenuEntity menu) {
        if (menu == null || menu.getBatches() == null || menu.getBatches().isEmpty()) {
            return Optional.empty();
        }

        return menu.getBatches().stream()
                .filter(batch -> batch.getStatus() == MenuStatus.APPROVED || batch.getStatus() == MenuStatus.SUBMITTED)
                .max(Comparator.comparing(MenuBatchEntity::getCreatedAt));
    }

    /**
     * MenuEntity-dən ən son batch-i tapır (status fərqli olmadan).
     *
     * @param menu MenuEntity
     * @return Ən son batch (optional)
     */
    public Optional<MenuBatchEntity> getLatestBatch(MenuEntity menu) {
        if (menu == null || menu.getBatches() == null || menu.getBatches().isEmpty()) {
            return Optional.empty();
        }

        return menu.getBatches().stream()
                .max(Comparator.comparing(MenuBatchEntity::getCreatedAt));
    }

    /**
     * User-in təsdiq gözləyən (SUBMITTED) batch-ini tapır.
     *
     * @param userEmail User email
     * @return SUBMITTED batch (optional)
     */
    public Optional<MenuBatchEntity> getPendingApprovalBatch(String userEmail) {
        return menuBatchRepository.findFirstByMenu_User_EmailAndStatus(userEmail, MenuStatus.SUBMITTED);
    }

    /**
     * MenuBatch-dən müəyyən günün item-lərini çıxarır və sıralayır.
     *
     * @param batch MenuBatch
     * @param day   Gün
     * @return Sorted menu items
     */
    public List<MenuItemEntity> getMenuItemsForDay(MenuBatchEntity batch, Integer day) {
        if (batch == null || batch.getItems() == null || day == null) {
            return List.of();
        }

        return batch.getItems().stream()
                .filter(item -> day.equals(item.getDay()))
                .sorted(Comparator.comparing(MenuItemEntity::getMealType))
                .collect(Collectors.toList());
    }

    /**
     * MenuBatch-in bütün item-lərini gün və meal type-a görə sıralayır.
     *
     * @param batch MenuBatch
     * @return Sorted menu items
     */
    public List<MenuItemEntity> getSortedMenuItems(MenuBatchEntity batch) {
        if (batch == null || batch.getItems() == null) {
            return List.of();
        }

        return batch.getItems().stream()
                .sorted(Comparator.comparing(MenuItemEntity::getDay)
                        .thenComparing(MenuItemEntity::getMealType))
                .collect(Collectors.toList());
    }

    /**
     * MenuBatch-in statusunu təyin edir.
     *
     * @param batch  MenuBatch
     * @param status Yeni status
     */
    public void updateBatchStatus(MenuBatchEntity batch, MenuStatus status) {
        batch.setStatus(status);
        menuBatchRepository.save(batch);
        log.info("Batch status yeniləndi: BatchId={}, NewStatus={}", batch.getId(), status);
    }

    /**
     * MenuBatch-i reject edir və səbəb əlavə edir.
     *
     * @param batch  MenuBatch
     * @param reason Rejection reason
     */
    public void rejectBatch(MenuBatchEntity batch, String reason) {
        batch.setStatus(MenuStatus.REJECTED);
        batch.setRejectionReason(reason);
        menuBatchRepository.save(batch);

        // Delivery-ləri sil
        deliveryRepository.deleteAllByBatchId(batch.getId());

        log.info("Batch reject edildi: BatchId={}, Reason={}", batch.getId(), reason);
    }
    /**
     * MenuBatch-i approve edir.
     *
     * @param batch MenuBatch
     */
    public void approveBatch(MenuBatchEntity batch) {
        batch.setStatus(MenuStatus.APPROVED);
        menuBatchRepository.save(batch);
        log.info("Batch təsdiqləndi: BatchId={}", batch.getId());
    }

    /**
     * User-in bütün menu-larını tapır (bütün aylar).
     *
     * @param userId User ID
     * @return Menu list
     */
    public List<MenuEntity> getAllMenusForUser(Long userId) {
        // Bu metod repository-də tətbiq edilməli
        log.warn("getAllMenusForUser: Repository metodunu implement edin");
        return List.of();
    }

    /**
     * Cari ayın statusunu təyin edir.
     *
     * @param user User
     * @return Cari ayın menu statusu
     */
    public MenuStatus getCurrentMonthMenuStatus(UserEntity user) {
        Optional<MenuEntity> currentMenu = getCurrentMonthMenu(user.getId());

        if (currentMenu.isEmpty()) {
            return MenuStatus.PREPARING;
        }

        Optional<MenuBatchEntity> activeBatch = getActiveBatch(currentMenu.get());

        return activeBatch
                .map(MenuBatchEntity::getStatus)
                .orElse(MenuStatus.PREPARING);
    }

    /**
     * MenuBatch-də unikal günlərin sayını hesablayır.
     *
     * @param batch MenuBatch
     * @return Unikal gün sayı
     */
    public long getUniqueDaysCount(MenuBatchEntity batch) {
        if (batch == null || batch.getItems() == null) {
            return 0;
        }

        return batch.getItems().stream()
                .map(MenuItemEntity::getDay)
                .distinct()
                .count();
    }

    /**
     * MenuBatch-də ümumi kalori hesablayır.
     *
     * @param batch MenuBatch
     * @return Ümumi kalori
     */
    public double getTotalCalories(MenuBatchEntity batch) {
        if (batch == null || batch.getItems() == null) {
            return 0.0;
        }

        return batch.getItems().stream()
                .filter(item -> item.getCalories() != null)
                .mapToDouble(MenuItemEntity::getCalories)
                .sum();
    }

    /**
     * MenuBatch-də müəyyən gündə neçə meal olduğunu hesablayır.
     *
     * @param batch MenuBatch
     * @param day   Gün
     * @return Meal sayı
     */
    public long getMealCountForDay(MenuBatchEntity batch, Integer day) {
        if (batch == null || batch.getItems() == null || day == null) {
            return 0;
        }

        return batch.getItems().stream()
                .filter(item -> day.equals(item.getDay()))
                .count();
    }

    /**
     * MenuBatch-in tam olub-olmadığını yoxlayır (hər gün üçün itemlər var).
     *
     * @param batch      MenuBatch
     * @param totalDays  Ayın ümumi günü
     * @return true əgər tamdırsa
     */
    public boolean isBatchComplete(MenuBatchEntity batch, int totalDays) {
        if (batch == null || batch.getItems() == null) {
            return false;
        }

        long uniqueDays = getUniqueDaysCount(batch);
        return uniqueDays == totalDays;
    }

    /**
     * Batch-in reject səbəbini qaytarır.
     *
     * @param batch MenuBatch
     * @return Rejection reason və ya default mesaj
     */
    public String getRejectionReason(MenuBatchEntity batch) {
        if (batch == null || batch.getRejectionReason() == null) {
            return "Səbəb göstərilməyib";
        }
        return batch.getRejectionReason();
    }
}