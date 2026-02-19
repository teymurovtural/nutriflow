package com.nutriflow.services.impl;

import com.nutriflow.dto.request.HealthDataRequest;
import com.nutriflow.dto.response.HealthDataResponse;
import com.nutriflow.entities.*;
import com.nutriflow.enums.UserStatus;
import com.nutriflow.helpers.EntityFinderHelper;
import com.nutriflow.mappers.HealthMapper;
import com.nutriflow.repositories.UserRepository;
import com.nutriflow.services.FileStorageService;
import com.nutriflow.services.HealthProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * HealthProfile Service Implementation (Refactored).
 * Professional code with EntityFinder and clean logging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthProfileServiceImpl implements HealthProfileService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // Helpers
    private final EntityFinderHelper entityFinder;

    // Mappers
    private final HealthMapper healthMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthDataResponse submitCompleteProfile(
            String email,
            HealthDataRequest request,
            List<MultipartFile> files) throws IOException {

        log.info("Health profile submission started: email={}", email);
        logRequestData(request, files);

        // 1. Find user
        UserEntity user = entityFinder.findUserByEmail(email);
        log.info("User found: userId={}", user.getId());

        // 2. HealthProfile - mövcuddursa update et, yoxdursa yeni yarat
        HealthProfileEntity healthProfile;
        if (user.getHealthProfile() != null) {
            healthProfile = healthMapper.updateHealthProfileEntity(request, user.getHealthProfile());
        } else {
            healthProfile = healthMapper.toHealthProfileEntity(request, user);
        }

        // 3. Address - mövcuddursa update et, yoxdursa yeni yarat
        AddressEntity address;
        if (user.getAddress() != null) {
            address = healthMapper.updateAddressEntity(request, user.getAddress());
        } else {
            address = healthMapper.toAddressEntity(request, user);
        }

        // 4. Set on user
        user.setHealthProfile(healthProfile);
        user.setAddress(address);
        user.setStatus(UserStatus.DATA_SUBMITTED);

        // 5. Save
        log.info("Saving user, HealthProfile and Address...");
        UserEntity savedUser = userRepository.save(user);
        log.info("First save completed. HealthProfileId={}", savedUser.getHealthProfile().getId());

        // 6. Process medical files
        if (hasFiles(files)) {
            processMedicalFiles(files, savedUser.getHealthProfile());
            userRepository.save(savedUser);
            log.info("Medical files saved. Count={}",
                    savedUser.getHealthProfile().getMedicalFiles().size());
        } else {
            log.warn("No medical file uploaded");
        }

        log.info("Health profile submission completed: userId={}, status={}",
                savedUser.getId(), savedUser.getStatus());

        return createSuccessResponse(savedUser);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Logs request and file data.
     */
    private void logRequestData(HealthDataRequest request, List<MultipartFile> files) {
        log.debug("Request: height={}, weight={}, goal={}",
                request.getHeight(), request.getWeight(), request.getGoal());

        if (files != null && !files.isEmpty()) {
            log.info("Number of files being uploaded: {}", files.size());
            files.forEach(file -> log.debug("File: name={}, size={} bytes, type={}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType()));
        }
    }

    /**
     * Checks whether the files list is empty.
     */
    private boolean hasFiles(List<MultipartFile> files) {
        return files != null && !files.isEmpty();
    }

    /**
     * Processes medical files and adds them to the HealthProfile.
     */
    private void processMedicalFiles(
            List<MultipartFile> files,
            HealthProfileEntity healthProfile) throws IOException {

        log.info("Processing medical files: count={}", files.size());

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            log.debug("Processing file #{}: name={}", i + 1, file.getOriginalFilename());

            // Write file to disk
            String filePath = fileStorageService.saveFile(file);
            log.debug("File written to disk: path={}", filePath);

            // Create MedicalFile entity
            MedicalFileEntity medicalFile = MedicalFileEntity.builder()
                    .healthProfile(healthProfile)
                    .fileUrl(filePath)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .build();

            // Add to HealthProfile
            healthProfile.getMedicalFiles().add(medicalFile);
            log.debug("MedicalFile entity created and added");
        }

        log.info("All medical files processed successfully");
    }

    /**
     * Creates a success response.
     */
    private HealthDataResponse createSuccessResponse(UserEntity user) {
        return healthMapper.toHealthDataResponse(
                user,
                "Data and files saved successfully."
        );
    }
}