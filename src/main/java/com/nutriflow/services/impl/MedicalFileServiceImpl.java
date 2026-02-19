package com.nutriflow.services.impl;

import com.nutriflow.dto.response.FileDownloadResponse;
import com.nutriflow.entities.MedicalFileEntity;
import com.nutriflow.entities.UserEntity;
import com.nutriflow.exceptions.FileStorageException;
import com.nutriflow.helpers.EntityFinderHelper;
import com.nutriflow.helpers.MedicalFileAccessHelper;
import com.nutriflow.mappers.MedicalFileMapper;
import com.nutriflow.security.SecurityUser;
import com.nutriflow.services.FileStorageService;
import com.nutriflow.services.MedicalFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalFileServiceImpl implements MedicalFileService {

    private final EntityFinderHelper entityFinderHelper;
    private final FileStorageService fileStorageService;
    private final MedicalFileMapper medicalFileMapper;
    private final MedicalFileAccessHelper medicalFileAccessHelper;;

    @Override
    public FileDownloadResponse getMedicalFileForDownload(Long fileId, UserDetails userDetails) {
        MedicalFileEntity medicalFile = medicalFileAccessHelper.resolveMedicalFile(fileId, userDetails);

        try {
            byte[] data = fileStorageService.loadFile(medicalFile.getFileUrl());
            return medicalFileMapper.toFileDownloadResponse(medicalFile, data);
        } catch (IOException e) {
            throw new FileStorageException("File could not be downloaded.", e);
        }
    }
}
