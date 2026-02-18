package com.nutriflow.helpers;

import com.nutriflow.dto.request.MenuItemRequest;
import com.nutriflow.entities.*;
import com.nutriflow.enums.MealType;
import com.nutriflow.enums.MenuStatus;
import com.nutriflow.repositories.MenuBatchRepository;
import com.nutriflow.repositories.MenuRepository;
import com.nutriflow.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class for MenuBatch operations.
 * Batch creation, update and management logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MenuBatchHelper {

    private final MenuRepository menuRepository;
    private final MenuBatchRepository menuBatchRepository;

    /**
     * Checks whether a user has an APPROVED batch for a given month.
     */
    public boolean hasApprovedMenu(Long userId, int year, int month) {
        return menuRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(menu -> menu.getBatches().stream()
                        .anyMatch(batch -> batch.getStatus() == MenuStatus.APPROVED))
                .orElse(false);
    }

    /**
     * Checks whether there are batches that require dietitian action.
     * Batches with DRAFT or REJECTED status require dietitian action.
     */
    public boolean isDietitianActionRequired(Long userId, int year, int month) {
        return menuRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(menu -> menu.getBatches().stream()
                        .max(Comparator.comparing(BaseEntity::getCreatedAt))
                        .map(batch -> batch.getStatus() == MenuStatus.DRAFT
                                || batch.getStatus() == MenuStatus.REJECTED)
                        .orElse(true)) // If no batch exists, action is required
                .orElse(true); // If no menu exists, must be created
    }

    /**
     * Finds or creates a Menu and/or Batch.
     * Finds a Draft batch, or creates a new one if not found.
     */
    @Transactional
    public MenuBatchEntity getOrCreateDraftBatch(
            UserEntity user,
            DietitianEntity dietitian,
            int year,
            int month) {

        log.info("Finding or creating draft batch: UserId={}, Year={}, Month={}",
                user.getId(), year, month);

        // 1. Find or create Menu
        MenuEntity menu = menuRepository.findByUserIdAndYearAndMonth(user.getId(), year, month)
                .orElseGet(() -> {
                    MenuEntity newMenu = MenuEntity.builder()
                            .user(user)
                            .dietitian(dietitian)
                            .year(year)
                            .month(month)
                            .batches(new ArrayList<>())
                            .build();
                    return menuRepository.save(newMenu);
                });

        // 2. Find or create Draft batch
        MenuBatchEntity draftBatch = menu.getBatches().stream()
                .filter(b -> b.getStatus() == MenuStatus.DRAFT)
                .findFirst()
                .orElseGet(() -> {
                    MenuBatchEntity newBatch = MenuBatchEntity.builder()
                            .menu(menu)
                            .status(MenuStatus.DRAFT)
                            .items(new ArrayList<>())
                            .build();
                    menu.getBatches().add(newBatch);
                    return newBatch;
                });

        log.info("Draft batch is ready: BatchId={}", draftBatch.getId());
        return draftBatch;
    }

    /**
     * Adds or updates items in a MenuBatch.
     */
    public void addOrUpdateItems(MenuBatchEntity batch, List<MenuItemRequest> itemRequests) {
        // Convert existing items to a map (key: "day-mealType")
        Map<String, MenuItemEntity> existingItemsMap = batch.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getDay() + "-" + item.getMealType(),
                        item -> item
                ));

        LocalDate today = LocalDate.now();

        for (MenuItemRequest itemRequest : itemRequests) {
            // Past date validation
            LocalDate targetDate = LocalDate.of(
                    batch.getMenu().getYear(),
                    batch.getMenu().getMonth(),
                    itemRequest.getDay()
            );

            if (DateUtils.isBeforeToday(targetDate)) {
                log.warn("A past date was submitted: {}", targetDate);
                throw new IllegalArgumentException(
                        "Day " + itemRequest.getDay() + " is in the past!");
            }

            String key = itemRequest.getDay() + "-" + itemRequest.getMealType();

            if (existingItemsMap.containsKey(key)) {
                // Update existing item
                updateMenuItem(existingItemsMap.get(key), itemRequest);
            } else {
                // Create new item
                batch.getItems().add(createMenuItem(batch, itemRequest));
            }
        }
    }

    /**
     * Updates a MenuItem.
     */
    private void updateMenuItem(MenuItemEntity item, MenuItemRequest request) {
        item.setDescription(request.getDescription());
        item.setCalories(request.getCalories());
        item.setProtein(request.getProtein());
        item.setCarbs(request.getCarbs());
        item.setFats(request.getFats());
    }

    /**
     * Creates a new MenuItem.
     */
    private MenuItemEntity createMenuItem(MenuBatchEntity batch, MenuItemRequest request) {
        return MenuItemEntity.builder()
                .batch(batch)
                .day(request.getDay())
                .mealType(request.getMealType())
                .description(request.getDescription())
                .calories(request.getCalories())
                .protein(request.getProtein())
                .carbs(request.getCarbs())
                .fats(request.getFats())
                .build();
    }

    /**
     * Sets the Batch status to SUBMITTED.
     */
    @Transactional
    public void submitBatch(MenuBatchEntity batch) {
        if (batch.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot submit an empty batch to the user.");
        }

        batch.setStatus(MenuStatus.SUBMITTED);
        menuBatchRepository.save(batch);

        log.info("Batch submitted: BatchId={}, ItemCount={}",
                batch.getId(), batch.getItems().size());
    }

    /**
     * Deletes a Batch or a specific item.
     */
    @Transactional
    public String deleteMenuContent(MenuBatchEntity batch, Integer day, MealType mealType) {
        // APPROVED batch cannot be deleted
        if (batch.getStatus() == MenuStatus.APPROVED) {
            throw new IllegalStateException(
                    "An approved menu cannot be deleted. It must be cancelled first.");
        }

        // Delete entire batch
        if (day == null && mealType == null) {
            menuBatchRepository.delete(batch);
            log.info("Batch deleted: BatchId={}", batch.getId());
            return "Menu batch with ID " + batch.getId() + " successfully deleted.";
        }

        // Delete specific items
        List<MenuItemEntity> items = batch.getItems();
        boolean removed = items.removeIf(item ->
                item.getDay().equals(day) && (mealType == null || item.getMealType() == mealType)
        );

        if (!removed) {
            return "No meal found matching the given criteria (Day: " + day + ").";
        }

        menuBatchRepository.save(batch);

        if (mealType != null) {
            log.info("Specific meal deleted: BatchId={}, Day={}, MealType={}",
                    batch.getId(), day, mealType);
            return "The " + mealType + " meal for day " + day + " of batch " + batch.getId() + " has been deleted.";
        } else {
            log.info("All meal items for the day deleted: BatchId={}, Day={}",
                    batch.getId(), day);
            return "All meals for day " + day + " of batch " + batch.getId() + " have been deleted.";
        }
    }

    /**
     * Updates a rejected batch.
     */
    @Transactional
    public void updateRejectedBatch(MenuBatchEntity batch, List<MenuItemRequest> newItems) {
        // Only APPROVED status is blocked
        if (batch.getStatus() == MenuStatus.APPROVED) {
            throw new IllegalStateException(
                    "An approved menu cannot be modified. Batch status: " + batch.getStatus());
        }

        log.info("Updating batch: BatchId={}, CurrentStatus={}", batch.getId(), batch.getStatus());

        // Add or update items
        addOrUpdateItems(batch, newItems);

        // Set status to DRAFT and clear rejection reason
        batch.setStatus(MenuStatus.DRAFT);
        batch.setRejectionReason(null);

        menuBatchRepository.save(batch);
        log.info("Batch updated and transitioned to DRAFT status");
    }

    /**
     * Checks whether a user has a REJECTED batch for a given month.
     */
    public boolean hasRejectedBatch(Long userId, int year, int month) {
        return menuRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(menu -> menu.getBatches().stream()
                        .anyMatch(batch -> batch.getStatus() == MenuStatus.REJECTED))
                .orElse(false);
    }

    /**
     * Finds the latest batch of a Menu.
     */
    public MenuBatchEntity getLatestBatch(MenuEntity menu) {
        return menu.getBatches().stream()
                .max(Comparator.comparing(BaseEntity::getCreatedAt))
                .orElse(null);
    }
}