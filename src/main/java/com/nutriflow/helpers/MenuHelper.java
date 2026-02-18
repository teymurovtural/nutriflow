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
 * Helper class for Menu and MenuBatch operations.
 * Menu filtering, batch selection and business logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MenuHelper {

    private final MenuRepository menuRepository;
    private final MenuBatchRepository menuBatchRepository;
    private final DeliveryRepository deliveryRepository;

    /**
     * Finds the current month's menu for a user.
     *
     * @param userId User ID
     * @return MenuEntity (optional)
     */
    public Optional<MenuEntity> getCurrentMonthMenu(Long userId) {
        LocalDate now = LocalDate.now();
        return menuRepository.findByUserIdAndYearAndMonth(userId, now.getYear(), now.getMonthValue());
    }

    /**
     * Finds a user's menu for a specific year and month.
     *
     * @param userId User ID
     * @param year   Year
     * @param month  Month
     * @return MenuEntity (optional)
     */
    public Optional<MenuEntity> getMenuByYearAndMonth(Long userId, int year, int month) {
        return menuRepository.findByUserIdAndYearAndMonth(userId, year, month);
    }

    /**
     * Finds the active batch (APPROVED or SUBMITTED) from a MenuEntity.
     *
     * @param menu MenuEntity
     * @return Active batch (optional)
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
     * Finds the latest batch from a MenuEntity (regardless of status).
     *
     * @param menu MenuEntity
     * @return Latest batch (optional)
     */
    public Optional<MenuBatchEntity> getLatestBatch(MenuEntity menu) {
        if (menu == null || menu.getBatches() == null || menu.getBatches().isEmpty()) {
            return Optional.empty();
        }

        return menu.getBatches().stream()
                .max(Comparator.comparing(MenuBatchEntity::getCreatedAt));
    }

    /**
     * Finds the user's batch pending approval (SUBMITTED).
     *
     * @param userEmail User email
     * @return SUBMITTED batch (optional)
     */
    public Optional<MenuBatchEntity> getPendingApprovalBatch(String userEmail) {
        return menuBatchRepository.findFirstByMenu_User_EmailAndStatus(userEmail, MenuStatus.SUBMITTED);
    }

    /**
     * Retrieves and sorts items for a specific day from a MenuBatch.
     *
     * @param batch MenuBatch
     * @param day   Day
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
     * Sorts all items in a MenuBatch by day and meal type.
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
     * Sets the status of a MenuBatch.
     *
     * @param batch  MenuBatch
     * @param status New status
     */
    public void updateBatchStatus(MenuBatchEntity batch, MenuStatus status) {
        batch.setStatus(status);
        menuBatchRepository.save(batch);
        log.info("Batch status updated: BatchId={}, NewStatus={}", batch.getId(), status);
    }

    /**
     * Rejects a MenuBatch and adds a reason.
     *
     * @param batch  MenuBatch
     * @param reason Rejection reason
     */
    public void rejectBatch(MenuBatchEntity batch, String reason) {
        batch.setStatus(MenuStatus.REJECTED);
        batch.setRejectionReason(reason);
        menuBatchRepository.save(batch);

        // Delete deliveries
        deliveryRepository.deleteAllByBatchId(batch.getId());

        log.info("Batch rejected: BatchId={}, Reason={}", batch.getId(), reason);
    }

    /**
     * Approves a MenuBatch.
     *
     * @param batch MenuBatch
     */
    public void approveBatch(MenuBatchEntity batch) {
        batch.setStatus(MenuStatus.APPROVED);
        menuBatchRepository.save(batch);
        log.info("Batch approved: BatchId={}", batch.getId());
    }

    /**
     * Finds all menus for a user (all months).
     *
     * @param userId User ID
     * @return Menu list
     */
    public List<MenuEntity> getAllMenusForUser(Long userId) {
        // This method should be implemented in the repository
        log.warn("getAllMenusForUser: Please implement the repository method");
        return List.of();
    }

    /**
     * Determines the current month's menu status.
     *
     * @param user User
     * @return Current month's menu status
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
     * Calculates the number of unique days in a MenuBatch.
     *
     * @param batch MenuBatch
     * @return Unique day count
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
     * Calculates total calories in a MenuBatch.
     *
     * @param batch MenuBatch
     * @return Total calories
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
     * Calculates the number of meals for a specific day in a MenuBatch.
     *
     * @param batch MenuBatch
     * @param day   Day
     * @return Meal count
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
     * Checks whether a MenuBatch is complete (items exist for every day).
     *
     * @param batch      MenuBatch
     * @param totalDays  Total days in the month
     * @return true if complete
     */
    public boolean isBatchComplete(MenuBatchEntity batch, int totalDays) {
        if (batch == null || batch.getItems() == null) {
            return false;
        }

        long uniqueDays = getUniqueDaysCount(batch);
        return uniqueDays == totalDays;
    }

    /**
     * Returns the rejection reason for a Batch.
     *
     * @param batch MenuBatch
     * @return Rejection reason or default message
     */
    public String getRejectionReason(MenuBatchEntity batch) {
        if (batch == null || batch.getRejectionReason() == null) {
            return "No reason provided";
        }
        return batch.getRejectionReason();
    }
}