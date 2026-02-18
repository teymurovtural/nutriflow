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
 * Clean code using SecurityUtils and DeliveryHelper.
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
        // Get current caterer ID via SecurityUtils
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Daily deliveries requested: CatererId={}, Date={}", catererId, date);

        // Find deliveries via Helper
        List<DeliveryEntity> deliveries = deliveryHelper.getDeliveriesByCatererAndDate(
                catererId, date, name, district
        );

        // Map to response
        return deliveries.stream()
                .map(delivery -> {
                    LocalDate deliveryDate = delivery.getDate();

                    // Find menu items for the given day via Helper
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

        log.info("Calculating dashboard statistics: CatererId={}", catererId);

        // Calculate statistics via Helper
        DeliveryHelper.CatererStatsData stats = deliveryHelper.calculateCatererStats(catererId, today);

        // Convert to response via Mapper
        return catererMapper.toStatsResponse(stats);
    }

    @Override
    @Transactional
    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus, String note) {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Updating delivery status: DeliveryId={}, NewStatus={}, CatererId={}",
                deliveryId, newStatus, catererId);

        // Find delivery
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IdNotFoundException("Order not found!"));

        // Security check
        validateCatererAccess(delivery, catererId);

        // Update status via Helper
        deliveryHelper.updateDeliveryStatus(delivery, newStatus, note);

        log.info("Delivery status updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public CatererResponse getProfile() {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Caterer profile requested: CatererId={}", catererId);

        CatererEntity caterer = catererRepository.findById(catererId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        // Convert to response via Mapper
        return catererMapper.toResponse(caterer);
    }

    @Override
    @Transactional
    public String updateProfile(CatererProfileUpdateRequest request) {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Updating caterer profile: CatererId={}", catererId);

        CatererEntity caterer = catererRepository.findById(catererId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found"));

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

        log.info("Profile updated successfully");
        return "Your profile information has been updated successfully";
    }

    @Override
    @Transactional
    public void updateEstimatedTime(Long deliveryId, String estimatedTime) {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Updating estimated time: DeliveryId={}, Time={}", deliveryId, estimatedTime);

        // Find delivery
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IdNotFoundException("Order not found!"));

        // Security check
        validateCatererAccess(delivery, catererId);

        // Update via Helper
        deliveryHelper.updateEstimatedTime(delivery, estimatedTime);

        log.info("Estimated time updated successfully");
    }

    @Override
    @Transactional
    public void markDeliveryAsFailed(DeliveryFailureRequest request) {
        Long catererId = SecurityUtils.getCurrentUserId();

        log.info("Marking delivery as failed: DeliveryId={}, CatererId={}, Reason={}",
                request.getDeliveryId(), catererId, request.getFailureReason());

        DeliveryEntity delivery = deliveryRepository.findById(request.getDeliveryId())
                .orElseThrow(() -> new IdNotFoundException("Delivery not found!"));

        validateCatererAccess(delivery, catererId);

        if (delivery.getStatus() == DeliveryStatus.FAILED) {
            throw new BusinessException("This delivery has already been marked as failed!");
        }
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new BusinessException("The status of a delivered order cannot be changed!");
        }

        delivery.setStatus(DeliveryStatus.FAILED);
        delivery.setCatererNote(request.getFailureReason() +
                (request.getNote() != null ? " | " + request.getNote() : ""));
        deliveryRepository.save(delivery);

        log.info("Delivery marked as failed: DeliveryId={}", request.getDeliveryId());
    }

    // ==================== Private Helper Methods ====================

    /**
     * Validates whether the caterer has access to the given delivery.
     */
    private void validateCatererAccess(DeliveryEntity delivery, Long catererId) {
        if (!delivery.getCaterer().getId().equals(catererId)) {
            log.warn("Caterer access denied: CatererId={}, DeliveryId={}", catererId, delivery.getId());
            throw new ResourceNotAvailableException("You do not have permission to access this order!");
        }
    }
}