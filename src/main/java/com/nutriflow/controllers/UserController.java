package com.nutriflow.controllers;

import com.nutriflow.dto.request.MenuApproveRequest;
import com.nutriflow.dto.request.UserProfileUpdateRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. Dashboard Data
    @GetMapping("/dashboard/summary")
    public ResponseEntity<UserDashboardResponse> getDashboardSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getDashboardSummary(userDetails.getUsername()));
    }

    // 2. View Current Menu
    @GetMapping("/my-menu")
    public ResponseEntity<List<MenuResponse>> getMyMenu(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyCurrentMenus(userDetails.getUsername()));
    }

    // 3. Approve Menu
    @PostMapping("/menu/approve")
    public ResponseEntity<String> approveMenu(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody(required = false) MenuApproveRequest request) { // @Valid added
        MenuApproveRequest finalRequest = (request != null) ? request : new MenuApproveRequest();
        userService.approveMenu(userDetails.getUsername(), finalRequest);
        return ResponseEntity.ok("Menu approved and delivery notes have been recorded.");
    }

    // 4. Reject Menu
    @PostMapping("/menu/reject")
    public ResponseEntity<String> rejectMenu(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long batchId,
            @RequestParam String reason) {
        // Note: If @Size is needed for reason, use @Size(min=5) before the parameter
        userService.rejectMenu(batchId, reason);
        return ResponseEntity.ok("Menu rejected. Your dietitian will make corrections.");
    }

    // 5. Medical Profile
    @GetMapping("/medical-profile")
    public ResponseEntity<PatientMedicalProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyMedicalProfile(userDetails.getUsername()));
    }

    // 6. Update Profile (Name, Address, Weight, etc.)
    @PutMapping("/profile/update")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request) { // @Valid added
        userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok("Your profile information has been updated.");
    }

    // 7. Cancel Subscription
    @PostMapping("/subscription/cancel")
    public ResponseEntity<String> cancelSubscription(@AuthenticationPrincipal UserDetails userDetails) {
        userService.cancelSubscription(userDetails.getUsername());
        return ResponseEntity.ok("Your subscription has been cancelled.");
    }

    // 8. Delivery Details
    @GetMapping("/deliveries")
    public ResponseEntity<List<DeliveryDetailResponse>> getMyDeliveries(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<DeliveryDetailResponse> deliveries = userService.getMyDeliveries(userDetails.getUsername());
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/subscription/info")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionInfoResponse> getMySubscriptionInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMySubscriptionInfo(userDetails.getUsername()));
    }

    @GetMapping("/personal-info")
    public ResponseEntity<UserPersonalInfoResponse> getMyPersonalInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyPersonalInfo(userDetails.getUsername()));
    }

}