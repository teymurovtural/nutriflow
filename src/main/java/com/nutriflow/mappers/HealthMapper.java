package com.nutriflow.mappers;

import com.nutriflow.dto.request.HealthDataRequest;
import com.nutriflow.dto.response.HealthDataResponse;
import com.nutriflow.entities.AddressEntity;
import com.nutriflow.entities.HealthProfileEntity;
import com.nutriflow.entities.UserEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Health və Address mapping.
 * Request to Entity və Entity to Response çevirməsi.
 */
@Component
public class HealthMapper {

    /**
     * HealthDataRequest-dən HealthProfileEntity yaradır.
     */
    public HealthProfileEntity toHealthProfileEntity(HealthDataRequest request, UserEntity user) {
        if (request == null) return null;

        return HealthProfileEntity.builder()
                .user(user)
                .height(request.getHeight())
                .weight(request.getWeight())
                .goal(request.getGoal())
                .restrictions(request.getRestrictions())
                .notes(request.getNotes())
                .medicalFiles(new ArrayList<>()) // Boş list ilə başladırıq
                .build();
    }

    /**
     * HealthDataRequest-dən AddressEntity yaradır.
     */
    public AddressEntity toAddressEntity(HealthDataRequest request, UserEntity user) {
        if (request == null) return null;

        return AddressEntity.builder()
                .user(user)
                .addressDetails(request.getAddressDetails())
                .city(request.getCity())
                .district(request.getDistrict())
                .deliveryNotes(request.getDeliveryNotes())
                .build();
    }

    /**
     * UserEntity-dən HealthDataResponse yaradır.
     * Health profile submit-dən sonra success response.
     */
    public HealthDataResponse toHealthDataResponse(UserEntity user, String message) {
        if (user == null) return null;

        return HealthDataResponse.builder()
                .message(message)
                .userEmail(user.getEmail())
                .newStatus(user.getStatus())
                .build();
    }
}