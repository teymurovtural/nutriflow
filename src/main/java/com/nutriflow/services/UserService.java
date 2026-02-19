package com.nutriflow.services;

import com.nutriflow.dto.request.MenuApproveRequest;
import com.nutriflow.dto.request.UserProfileUpdateRequest;
import com.nutriflow.dto.response.*;
import java.util.List;

public interface UserService {

    UserDashboardResponse getDashboardSummary(String email);

    PatientMedicalProfileResponse getMyMedicalProfile(String email);

    List<MenuResponse> getMyCurrentMenus(String email);

    void rejectMenu(Long batchId, String reason); // Updated with BatchId

    void approveMenu(String email, MenuApproveRequest request);

    void updateProfile(String email, UserProfileUpdateRequest request);

    void cancelSubscription(String email);

    List<DeliveryDetailResponse> getMyDeliveries(String email);

    SubscriptionInfoResponse getMySubscriptionInfo(String email);

    UserPersonalInfoResponse getMyPersonalInfo(String email);

}