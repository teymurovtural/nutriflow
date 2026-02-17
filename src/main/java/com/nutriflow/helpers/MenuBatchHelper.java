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
 * MenuBatch əməliyyatları üçün helper sinif.
 * Batch creation, update və management logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MenuBatchHelper {

    private final MenuRepository menuRepository;
    private final MenuBatchRepository menuBatchRepository;

    /**
     * User-in müəyyən ay üçün APPROVED statuslu batch-i olub-olmadığını yoxlayır.
     */
    public boolean hasApprovedMenu(Long userId, int year, int month) {
        return menuRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(menu -> menu.getBatches().stream()
                        .anyMatch(batch -> batch.getStatus() == MenuStatus.APPROVED))
                .orElse(false);
    }

    /**
     * Dietitian action tələb edən batch-lər olub-olmadığını yoxlayır.
     * DRAFT və ya REJECTED statuslu batch-lər dietitian action tələb edir.
     */
    public boolean isDietitianActionRequired(Long userId, int year, int month) {
        return menuRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(menu -> menu.getBatches().stream()
                        .max(Comparator.comparing(BaseEntity::getCreatedAt))
                        .map(batch -> batch.getStatus() == MenuStatus.DRAFT
                                || batch.getStatus() == MenuStatus.REJECTED)
                        .orElse(true)) // Heç batch yoxdursa, action lazımdır
                .orElse(true); // Menyu yoxdursa, mütləq yaradılmalıdır
    }

    /**
     * Menu və ya Batch tapır, yoxdursa yaradır.
     * Draft batch tapır, yoxdursa yeni yaradır.
     */
    @Transactional
    public MenuBatchEntity getOrCreateDraftBatch(
            UserEntity user,
            DietitianEntity dietitian,
            int year,
            int month) {

        log.info("Draft batch tapılır və ya yaradılır: UserId={}, Year={}, Month={}",
                user.getId(), year, month);

        // 1. Menu tap və ya yarat
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

        // 2. Draft batch tap və ya yarat
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

        log.info("Draft batch hazırdır: BatchId={}", draftBatch.getId());
        return draftBatch;
    }

    /**
     * MenuBatch-ə item əlavə edir və ya mövcud item-i update edir.
     */
    public void addOrUpdateItems(MenuBatchEntity batch, List<MenuItemRequest> itemRequests) {
        // Mövcud itemləri map-ə çeviririk (key: "day-mealType")
        Map<String, MenuItemEntity> existingItemsMap = batch.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getDay() + "-" + item.getMealType(),
                        item -> item
                ));

        LocalDate today = LocalDate.now();

        for (MenuItemRequest itemRequest : itemRequests) {
            // Keçmiş tarix yoxlaması
            LocalDate targetDate = LocalDate.of(
                    batch.getMenu().getYear(),
                    batch.getMenu().getMonth(),
                    itemRequest.getDay()
            );

            if (DateUtils.isBeforeToday(targetDate)) {
                log.warn("Keçmiş tarix göndərildi: {}", targetDate);
                throw new IllegalArgumentException(
                        itemRequest.getDay() + " tarixi keçmişdə qalıb!");
            }

            String key = itemRequest.getDay() + "-" + itemRequest.getMealType();

            if (existingItemsMap.containsKey(key)) {
                // Mövcud item-i update et
                updateMenuItem(existingItemsMap.get(key), itemRequest);
            } else {
                // Yeni item yarat
                batch.getItems().add(createMenuItem(batch, itemRequest));
            }
        }
    }

    /**
     * MenuItem-i update edir.
     */
    private void updateMenuItem(MenuItemEntity item, MenuItemRequest request) {
        item.setDescription(request.getDescription());
        item.setCalories(request.getCalories());
        item.setProtein(request.getProtein());
        item.setCarbs(request.getCarbs());
        item.setFats(request.getFats());
    }

    /**
     * Yeni MenuItem yaradır.
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
     * Batch-i SUBMITTED statusuna çevirir.
     */
    @Transactional
    public void submitBatch(MenuBatchEntity batch) {
        if (batch.getItems().isEmpty()) {
            throw new IllegalStateException("Boş paketi istifadəçiyə təqdim edə bilməzsiniz.");
        }

        batch.setStatus(MenuStatus.SUBMITTED);
        menuBatchRepository.save(batch);

        log.info("Batch submit edildi: BatchId={}, ItemCount={}",
                batch.getId(), batch.getItems().size());
    }

    /**
     * Batch-i və ya specific item-i silir.
     */
    @Transactional
    public String deleteMenuContent(MenuBatchEntity batch, Integer day, MealType mealType) {
        // APPROVED batch silinə bilməz
        if (batch.getStatus() == MenuStatus.APPROVED) {
            throw new IllegalStateException(
                    "Təsdiqlənmiş menyu silinə bilməz. Əvvəlcə ləğv edilməlidir.");
        }

        // Bütün batch-i sil
        if (day == null && mealType == null) {
            menuBatchRepository.delete(batch);
            log.info("Batch silindi: BatchId={}", batch.getId());
            return "Batch ID " + batch.getId() + " olan bütün menyu paketi uğurla silindi.";
        }

        // Specific item-ləri sil
        List<MenuItemEntity> items = batch.getItems();
        boolean removed = items.removeIf(item ->
                item.getDay().equals(day) && (mealType == null || item.getMealType() == mealType)
        );

        if (!removed) {
            return "Göstərilən kriteriyalara uyğun yemək tapılmadı (Gün: " + day + ").";
        }

        menuBatchRepository.save(batch);

        if (mealType != null) {
            log.info("Specific meal silindi: BatchId={}, Day={}, MealType={}",
                    batch.getId(), day, mealType);
            return batch.getId() + " nömrəli paketin " + day + ". gününün "
                    + mealType + " yeməyi silindi.";
        } else {
            log.info("Günün bütün yemək itemləri silindi: BatchId={}, Day={}",
                    batch.getId(), day);
            return batch.getId() + " nömrəli paketin " + day
                    + ". gününə aid bütün yeməklər silindi.";
        }
    }

    /**
     * Rejected batch-i yeniləyir.
     */
    @Transactional
    public void updateRejectedBatch(MenuBatchEntity batch, List<MenuItemRequest> newItems) {
        // YALNIZ APPROVED statusu bloklanır
        if (batch.getStatus() == MenuStatus.APPROVED) {
            throw new IllegalStateException(
                    "Təsdiqlənmiş menyu dəyişdirilə bilməz. Paket statusu: " + batch.getStatus());
        }

        log.info("Batch yenilənir: BatchId={}, CurrentStatus={}", batch.getId(), batch.getStatus());

        // Item-ləri əlavə və ya update et
        addOrUpdateItems(batch, newItems);

        // Statusu DRAFT et, rejection reason-u təmizlə
        batch.setStatus(MenuStatus.DRAFT);
        batch.setRejectionReason(null);

        menuBatchRepository.save(batch);
        log.info("Batch yeniləndi və DRAFT statusuna keçdi");
    }

    /**
     * User-in müəyyən ay üçün REJECTED batch-i olub-olmadığını yoxlayır.
     */
    public boolean hasRejectedBatch(Long userId, int year, int month) {
        return menuRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(menu -> menu.getBatches().stream()
                        .anyMatch(batch -> batch.getStatus() == MenuStatus.REJECTED))
                .orElse(false);
    }

    /**
     * Menu-nun ən son batch-ini tapır.
     */
    public MenuBatchEntity getLatestBatch(MenuEntity menu) {
        return menu.getBatches().stream()
                .max(Comparator.comparing(BaseEntity::getCreatedAt))
                .orElse(null);
    }
}