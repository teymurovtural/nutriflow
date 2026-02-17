package com.nutriflow.utils;

import com.nutriflow.constants.FileConstants;
import com.nutriflow.exceptions.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * Fayl validation əməliyyatları üçün utility class
 */
@Slf4j
public final class FileValidationUtil {

    private FileValidationUtil() {
        throw new UnsupportedOperationException("Bu utility class-dır, instantiate edilə bilməz");
    }

    /**
     * Faylın bütün validation-larını yerinə yetirir
     *
     * @param file yoxlanılacaq fayl
     * @throws FileUploadException validation uğursuz olduqda
     */
    public static void validateFile(MultipartFile file) {
        log.debug("Fayl validation başladı: {}", file.getOriginalFilename());

        validateFileNotEmpty(file);
        validateFileSize(file);
        validateFileType(file);

        log.debug("Fayl validation uğurla tamamlandı: {}", file.getOriginalFilename());
    }

    /**
     * Faylın boş olmadığını yoxlayır
     *
     * @param file yoxlanılacaq fayl
     * @throws FileUploadException fayl boş olduqda
     */
    private static void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Validation uğursuz: Fayl boşdur");
            throw new FileUploadException(FileConstants.ERROR_EMPTY_FILE);
        }
    }

    /**
     * Fayl ölçüsünün limit daxilində olduğunu yoxlayır
     *
     * @param file yoxlanılacaq fayl
     * @throws FileUploadException fayl çox böyük olduqda
     */
    private static void validateFileSize(MultipartFile file) {
        if (file.getSize() > FileConstants.MAX_FILE_SIZE) {
            log.warn("Validation uğursuz: Fayl ölçüsü limiti aşır. Ölçü: {} bytes, Limit: {} bytes",
                    file.getSize(), FileConstants.MAX_FILE_SIZE);
            throw new FileUploadException(FileConstants.ERROR_FILE_TOO_LARGE);
        }
    }

    /**
     * Fayl tipinin icazə verilən formatlardan olduğunu yoxlayır
     *
     * @param file yoxlanılacaq fayl
     * @throws FileUploadException fayl tipi icazə verilməyən olduqda
     */
    private static void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !FileConstants.ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Validation uğursuz: İcazə verilməyən format - {}", contentType);
            throw new FileUploadException(FileConstants.ERROR_INVALID_FORMAT);
        }

        log.debug("Fayl tipi yoxlanıldı və təsdiqləndi: {}", contentType);
    }

    /**
     * Fayl extension-ının düzgün olub olmadığını yoxlayır (əlavə təhlükəsizlik)
     *
     * @param fileName fayl adı
     * @return true - düzgün extension, false - yanlış extension
     */
    public static boolean hasValidExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        String lowerCaseFileName = fileName.toLowerCase();
        return FileConstants.ALLOWED_FILE_EXTENSIONS.stream()
                .anyMatch(lowerCaseFileName::endsWith);
    }
}