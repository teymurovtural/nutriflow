package com.nutriflow.utils;

import com.nutriflow.constants.FileConstants;
import com.nutriflow.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Fayl əməliyyatları üçün utility class
 */
@Slf4j
public final class FileOperationUtil {

    private FileOperationUtil() {
        throw new UnsupportedOperationException("Bu utility class-dır, instantiate edilə bilməz");
    }

    /**
     * Unikal fayl adı yaradır
     *
     * @param originalFileName orijinal fayl adı
     * @return UUID ilə birlikdə yeni unikal fayl adı
     */
    public static String generateUniqueFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            log.warn("Original fayl adı boşdur, default ad istifadə olunur");
            originalFileName = "file";
        }

        String uniqueFileName = UUID.randomUUID().toString()
                + FileConstants.FILE_NAME_SEPARATOR
                + originalFileName;

        log.debug("Unikal fayl adı yaradıldı: {}", uniqueFileName);
        return uniqueFileName;
    }

    /**
     * Qovluğun mövcudluğunu yoxlayır, yoxdursa yaradır
     *
     * @param directoryPath qovluq yolu
     * @throws FileStorageException qovluq yaradıla bilmədikdə
     */
    public static void ensureDirectoryExists(String directoryPath) {
        Path path = Paths.get(directoryPath);

        if (!Files.exists(path)) {
            try {
                log.info(FileConstants.LOG_UPLOAD_DIR_CREATED, directoryPath);
                Files.createDirectories(path);
                log.debug("Qovluq uğurla yaradıldı: {}", directoryPath);
            } catch (IOException e) {
                log.error("Qovluq yaradılarkən xəta: {}", e.getMessage());
                throw new FileStorageException("Qovluq yaradıla bilmədi: " + directoryPath, e);
            }
        }
    }

    /**
     * Fayl yolunun düzgün olub olmadığını yoxlayır
     *
     * @param filePath fayl yolu
     * @return true - düzgün yol, false - yanlış yol
     */
    public static boolean isValidFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            // Path normalization ilə yoxlama
            path.normalize();
            return true;
        } catch (Exception e) {
            log.warn("Yanlış fayl yolu: {}", filePath);
            return false;
        }
    }

    /**
     * Faylın mövcud olub olmadığını yoxlayır
     *
     * @param filePath fayl yolu
     * @return true - fayl mövcuddur, false - fayl yoxdur
     */
    public static boolean fileExists(String filePath) {
        if (!isValidFilePath(filePath)) {
            return false;
        }

        Path path = Paths.get(filePath);
        return Files.exists(path);
    }

    /**
     * Fayl adından extension-ı əldə edir
     *
     * @param fileName fayl adı
     * @return extension (məs: .pdf, .jpg) və ya boş string
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }

        return "";
    }

    /**
     * Fayl yolunu Path obyektinə çevirir
     *
     * @param filePath fayl yolu
     * @return Path obyekti
     * @throws FileStorageException yol düzgün olmadıqda
     */
    public static Path toPath(String filePath) {
        if (!isValidFilePath(filePath)) {
            log.error("Düzgün olmayan fayl yolu: {}", filePath);
            throw new FileStorageException(FileConstants.ERROR_INVALID_FILE_PATH);
        }

        return Paths.get(filePath);
    }
}