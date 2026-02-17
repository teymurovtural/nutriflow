package com.nutriflow.services.impl;

import com.nutriflow.dto.request.CatererProfileUpdateRequest;
import com.nutriflow.dto.request.DeliveryFailureRequest;
import com.nutriflow.dto.response.CatererResponse;
import com.nutriflow.dto.response.CatererStatsResponse;
import com.nutriflow.dto.response.DeliveryDetailResponse;
import com.nutriflow.entities.CatererEntity;
import com.nutriflow.entities.DeliveryEntity;
import com.nutriflow.entities.MenuItemEntity;
import com.nutriflow.enums.DeliveryStatus;
import com.nutriflow.exceptions.*;
import com.nutriflow.helpers.DeliveryHelper;
import com.nutriflow.mappers.CatererMapper;
import com.nutriflow.mappers.DeliveryMapper;
import com.nutriflow.repositories.CatererRepository;
import com.nutriflow.repositories.DeliveryRepository;
import com.nutriflow.services.CatererService;
import com.nutriflow.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Caterer Service Implementation (Refactored).
 * SecurityUtils və DeliveryHelper istifadə edərək təmiz kod.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatererServiceImpl implements CatererService {

    private final DeliveryRepository deliveryRepository;
    private final CatererRepository catererRepository;
    private final PasswordEncoder passwordEncoder;

    // Helpers
    private final DeliveryHelper deliveryHelper;

    // Mappers
    private final DeliveryMapper deliveryMapper;
    private final CatererMapper catererMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryDetailResponse> getDailyDeliveries(String name, String district, LocalDate date) {
        // SecurityUtils ilə current caterer ID-sini al
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Günlük delivery-lər istənilir: CatererId={}, Date={}", catererId, date);

        // Helper ilə deliverylə tap
        List<DeliveryEntity> deliveries = deliveryHelper.getDeliveriesByCatererAndDate(
                catererId, date, name, district
        );

        // Map to response
        return deliveries.stream()
                .map(delivery -> {
                    LocalDate deliveryDate = delivery.getDate();

                    // Helper ilə həmin günün menu items-ini tap
                    List<MenuItemEntity> itemsOfSelectedDay = deliveryHelper.getMenuItemsForDay(
                            delivery.getBatch(),
                            deliveryDate.getDayOfMonth()
                    );

                    return deliveryMapper.toDetailResponse(delivery, itemsOfSelectedDay);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CatererStatsResponse getDashboardStats() {
        Long catererId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now();

        log.info("Dashboard statistikası hesablanır: CatererId={}", catererId);

        // Helper ilə statistikası hesabla
        DeliveryHelper.CatererStatsData stats = deliveryHelper.calculateCatererStats(catererId, today);

        // Mapper ilə response-a çevir
        return catererMapper.toStatsResponse(stats);
    }

    @Override
    @Transactional
    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus, String note) {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Delivery status yenilənir: DeliveryId={}, NewStatus={}, CatererId={}",
                deliveryId, newStatus, catererId);

        // Delivery-ni tap
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IdNotFoundException("Sifariş tapılmadı!"));

        // Təhlükəsizlik yoxlaması
        validateCatererAccess(delivery, catererId);

        // Helper ilə status yenilə
        deliveryHelper.updateDeliveryStatus(delivery, newStatus, note);

        log.info("Delivery status uğurla yeniləndi");
    }

    @Override
    @Transactional(readOnly = true)
    public CatererResponse getProfile() {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Caterer profili istənilir: CatererId={}", catererId);

        CatererEntity caterer = catererRepository.findById(catererId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil tapılmadı"));

        // Mapper ilə response-a çevir
        return catererMapper.toResponse(caterer);
    }

    @Override
    @Transactional
    public String updateProfile(CatererProfileUpdateRequest request) {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Caterer profili yenilənir: CatererId={}", catererId);

        CatererEntity caterer = catererRepository.findById(catererId)
                .orElseThrow(() -> new UserNotFoundException("Profil tapılmadı"));

        // Update fields
        if (request.getName() != null) caterer.setName(request.getName());
        if (request.getPhone() != null) caterer.setPhone(request.getPhone());
        if (request.getAddress() != null) caterer.setAddress(request.getAddress());

        if (request.getEmail() != null && !request.getEmail().equals(caterer.getEmail())) {
            caterer.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            caterer.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        catererRepository.save(caterer);

        log.info("Profil uğurla yeniləndi");
        return "Profil məlumatlarınız uğurla yeniləndi";
    }

    @Override
    @Transactional
    public void updateEstimatedTime(Long deliveryId, String estimatedTime) {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Estimated time yenilənir: DeliveryId={}, Time={}", deliveryId, estimatedTime);

        // Delivery-ni tap
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IdNotFoundException("Sifariş tapılmadı!"));

        // Təhlükəsizlik yoxlaması
        validateCatererAccess(delivery, catererId);

        // Helper ilə yenilə
        deliveryHelper.updateEstimatedTime(delivery, estimatedTime);

        log.info("Estimated time uğurla yeniləndi");
    }

    @Override
    @Transactional
    public void markDeliveryAsFailed(DeliveryFailureRequest request) {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Çatdırılma uğursuz işarələnir: DeliveryId={}, CatererId={}, Səbəb={}",
                request.getDeliveryId(), catererId, request.getFailureReason());

        DeliveryEntity delivery = deliveryRepository.findById(request.getDeliveryId())
                .orElseThrow(() -> new IdNotFoundException("Çatdırılma tapılmadı!"));

        validateCatererAccess(delivery, catererId);

        if (delivery.getStatus() == DeliveryStatus.FAILED) {
            throw new BusinessException("Bu çatdırılma artıq uğursuz kimi işarələnib!");
        }
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new BusinessException("Çatdırılmış sifarişin statusu dəyişdirilə bilməz!");
        }

        delivery.setStatus(DeliveryStatus.FAILED);
        delivery.setCatererNote(request.getFailureReason() +
                (request.getNote() != null ? " | " + request.getNote() : ""));
        deliveryRepository.save(delivery);

        log.info("Çatdırılma uğursuz kimi işarələndi: DeliveryId={}", request.getDeliveryId());
    }

    // ==================== Private Helper Methods ====================

    /**
     * Caterer-in delivery-yə access hüququnu yoxlayır.
     */
    private void validateCatererAccess(DeliveryEntity delivery, Long catererId) {
        if (!delivery.getCaterer().getId().equals(catererId)) {
            log.warn("Caterer access denied: CatererId={}, DeliveryId={}", catererId, delivery.getId());
            throw new ResourceNotAvailableException("Bu sifarişə müdaxilə icazəniz yoxdur!");
        }
    }
}