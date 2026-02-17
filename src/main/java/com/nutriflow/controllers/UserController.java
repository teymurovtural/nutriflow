package com.nutriflow.controllers;

import com.nutriflow.dto.request.MenuApproveRequest;
import com.nutriflow.dto.request.UserProfileUpdateRequest;
import com.nutriflow.dto.response.DeliveryDetailResponse;
import com.nutriflow.dto.response.MenuResponse;
import com.nutriflow.dto.response.PatientMedicalProfileResponse;
import com.nutriflow.dto.response.UserDashboardResponse;
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

    // 1. Dashboard Məlumatları
    @GetMapping("/dashboard/summary")
    public ResponseEntity<UserDashboardResponse> getDashboardSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getDashboardSummary(userDetails.getUsername()));
    }

    // 2. Cari Menyunu Görmək
    @GetMapping("/my-menu")
    public ResponseEntity<MenuResponse> getMyMenu(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyCurrentMenu(userDetails.getUsername()));
    }

    // 3. Menyunu Təsdiqləmək
    @PostMapping("/menu/approve")
    public ResponseEntity<String> approveMenu(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody(required = false) MenuApproveRequest request) { // @Valid əlavə edildi
        MenuApproveRequest finalRequest = (request != null) ? request : new MenuApproveRequest();
        userService.approveMenu(userDetails.getUsername(), finalRequest);
        return ResponseEntity.ok("Menyu təsdiqləndi və çatdırılma qeydləri qeydə alındı.");
    }

    // 4. Menyunu İmtina Etmək
    @PostMapping("/menu/reject")
    public ResponseEntity<String> rejectMenu(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long batchId,
            @RequestParam String reason) {
        // Qeyd: Əgər reason üçün @Size lazımdırsa, parametr qarşısında @Size(min=5) istifadə edə bilərsən
        userService.rejectMenu(batchId, reason);
        return ResponseEntity.ok("Menyu imtina edildi. Dietoloqunuz düzəliş edəcək.");
    }

    // 5. Tibbi Profil
    @GetMapping("/medical-profile")
    public ResponseEntity<PatientMedicalProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyMedicalProfile(userDetails.getUsername()));
    }

    // 6. Profil Yeniləmə (Ad, Ünvan, Çəki və s.)
    @PutMapping("/profile/update")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request) { // @Valid əlavə edildi
        userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok("Profil məlumatlarınız yeniləndi.");
    }

    // 7. Abunəliyi Dayandırmaq
    @PostMapping("/subscription/cancel")
    public ResponseEntity<String> cancelSubscription(@AuthenticationPrincipal UserDetails userDetails) {
        userService.cancelSubscription(userDetails.getUsername());
        return ResponseEntity.ok("Abunəliyiniz ləğv edildi.");
    }

    // 8. Çatdırılmanın detalları
    @GetMapping("/deliveries")
    public ResponseEntity<List<DeliveryDetailResponse>> getMyDeliveries(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<DeliveryDetailResponse> deliveries = userService.getMyDeliveries(userDetails.getUsername());
        return ResponseEntity.ok(deliveries);
    }
}