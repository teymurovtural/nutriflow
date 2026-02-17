package com.nutriflow.services.impl;

import com.nutriflow.constants.FileConstants;
import com.nutriflow.exceptions.FileStorageException;
import com.nutriflow.services.FileStorageService;
import com.nutriflow.utils.FileOperationUtil;
import com.nutriflow.utils.FileValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Fayl saxlama və idarəetmə service implementasiyası
 *
 * Bu service faylların diskdə saxlanılması və silinməsi üçün istifadə olunur.
 * Dəstəklənən formatlar: PDF, JPG, PNG
 * Maksimum fayl ölçüsü: 10MB
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Faylı diskə saxlayır
     *
     * @param file saxlanılacaq fayl
     * @return saxlanılan faylın tam yolu
     * @throws IOException fayl saxlanılarkən xəta baş verdikdə
     * @throws FileStorageException validation və ya storage xətası baş verdikdə
     */
    @Override
    public String saveFile(MultipartFile file) throws IOException {
        log.info(FileConstants.LOG_FILE_UPLOAD_STARTED,
                file.getOriginalFilename(), file.getSize());

        // 1. Faylı validate et
        FileValidationUtil.validateFile(file);

        // 2. Upload directory-ni hazırla
        FileOperationUtil.ensureDirectoryExists(uploadDir);

        // 3. Unikal fayl adı yarat
        String uniqueFileName = FileOperationUtil.generateUniqueFileName(
                file.getOriginalFilename());

        // 4. Tam fayl yolunu müəyyən et
        Path uploadPath = FileOperationUtil.toPath(uploadDir);
        Path filePath = uploadPath.resolve(uniqueFileName);

        // 5. Faylı diskə yaz
        saveFileToDisk(file, filePath);

        log.info(FileConstants.LOG_FILE_SAVED_SUCCESS, uniqueFileName);
        return filePath.toString();
    }

    /**
     * Faylı fiziki olaraq diskə yazır
     *
     * @param file yazılacaq fayl
     * @param targetPath hədəf fayl yolu
     * @throws IOException yazma əməliyyatında xəta baş verdikdə
     */
    private void saveFileToDisk(MultipartFile file, Path targetPath) throws IOException {
        try {
            log.debug("Fayl diskə yazılır: {}", targetPath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Fayl diskə yazılanda xəta baş verdi: {}", e.getMessage(), e);
            throw new FileStorageException(FileConstants.ERROR_FILE_SAVE_FAILED, e);
        }
    }

    /**
     * Faylı diskdən silir
     *
     * @param filePath silinəcək faylın yolu
     * @throws IOException fayl silinərkən xəta baş verdikdə
     * @throws FileStorageException yol düzgün olmadıqda
     */
    @Override
    public void deleteFile(String filePath) throws IOException {
        log.info("Faylın silinməsi sorğusu: {}", filePath);

        // Fayl yolunu validate et
        if (!FileOperationUtil.isValidFilePath(filePath)) {
            log.error("Düzgün olmayan fayl yolu: {}", filePath);
            throw new FileStorageException(FileConstants.ERROR_INVALID_FILE_PATH);
        }

        Path path = FileOperationUtil.toPath(filePath);

        try {
            boolean deleted = Files.deleteIfExists(path);

            if (deleted) {
                log.info(FileConstants.LOG_FILE_DELETED_SUCCESS, filePath);
            } else {
                log.warn(FileConstants.LOG_FILE_NOT_FOUND, filePath);
            }
        } catch (IOException e) {
            log.error("Fayl silinərkən xəta baş verdi: {}", e.getMessage(), e);
            throw new FileStorageException(FileConstants.ERROR_FILE_DELETE_FAILED, e);
        }
    }
}