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
 * EntityFinder və təmiz logging ilə professional kod.
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

        log.info("Health profile submit başladı: email={}", email);
        logRequestData(request, files);

        // 1. User-i tap
        UserEntity user = entityFinder.findUserByEmail(email);
        log.info("User tapıldı: userId={}", user.getId());

        // 2. HealthProfile və Address yarat
        HealthProfileEntity healthProfile = healthMapper.toHealthProfileEntity(request, user);
        AddressEntity address = healthMapper.toAddressEntity(request, user);

        // 3. User-ə set et
        user.setHealthProfile(healthProfile);
        user.setAddress(address);
        user.setStatus(UserStatus.DATA_SUBMITTED);

        // 4. İlk save - healthProfile ID yaranması üçün
        log.info("User, HealthProfile və Address save edilir...");
        UserEntity savedUser = userRepository.save(user);
        log.info("İlk save tamamlandı. HealthProfileId={}", savedUser.getHealthProfile().getId());

        // 5. Medical files-ı emal et
        if (hasFiles(files)) {
            processMedicalFiles(files, savedUser.getHealthProfile());

            // İkinci save - medical files ilə
            userRepository.save(savedUser);
            log.info("Medical files save edildi. Count={}",
                    savedUser.getHealthProfile().getMedicalFiles().size());
        } else {
            log.warn("Medical file yüklənmədi");
        }

        log.info("Health profile submit tamamlandı: userId={}, status={}",
                savedUser.getId(), savedUser.getStatus());

        // Mapper ilə response yarat
        return createSuccessResponse(savedUser);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Request və files-ı log edir.
     */
    private void logRequestData(HealthDataRequest request, List<MultipartFile> files) {
        log.debug("Request: height={}, weight={}, goal={}",
                request.getHeight(), request.getWeight(), request.getGoal());

        if (files != null && !files.isEmpty()) {
            log.info("Yüklənən file sayı: {}", files.size());
            files.forEach(file -> log.debug("File: name={}, size={} bytes, type={}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType()));
        }
    }

    /**
     * Files list-inin boş olub-olmadığını yoxlayır.
     */
    private boolean hasFiles(List<MultipartFile> files) {
        return files != null && !files.isEmpty();
    }

    /**
     * Medical files-ı emal edir və HealthProfile-ə əlavə edir.
     */
    private void processMedicalFiles(
            List<MultipartFile> files,
            HealthProfileEntity healthProfile) throws IOException {

        log.info("Medical files emal edilir: count={}", files.size());

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            log.debug("File #{} emal olunur: name={}", i + 1, file.getOriginalFilename());

            // File-ı diskə yaz
            String filePath = fileStorageService.saveFile(file);
            log.debug("File diskə yazıldı: path={}", filePath);

            // MedicalFile entity yarat
            MedicalFileEntity medicalFile = MedicalFileEntity.builder()
                    .healthProfile(healthProfile)
                    .fileUrl(filePath)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .build();

            // HealthProfile-ə əlavə et
            healthProfile.getMedicalFiles().add(medicalFile);
            log.debug("MedicalFile entity yaradıldı və əlavə edildi");
        }

        log.info("Bütün medical files uğurla emal edildi");
    }

    /**
     * Success response yaradır.
     */
    private HealthDataResponse createSuccessResponse(UserEntity user) {
        return healthMapper.toHealthDataResponse(
                user,
                "Məlumatlar və fayllar uğurla yadda saxlanıldı."
        );
    }
}