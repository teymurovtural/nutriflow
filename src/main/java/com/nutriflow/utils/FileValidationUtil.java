package com.nutriflow.utils;

import com.nutriflow.constants.FileConstants;
import com.nutriflow.exceptions.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for file validation operations
 */
@Slf4j
public final class FileValidationUtil {

    private FileValidationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Performs all validations on a file
     *
     * @param file file to be validated
     * @throws FileUploadException if validation fails
     */
    public static void validateFile(MultipartFile file) {
        log.debug("File validation started: {}", file.getOriginalFilename());

        validateFileNotEmpty(file);
        validateFileSize(file);
        validateFileType(file);

        log.debug("File validation completed successfully: {}", file.getOriginalFilename());
    }

    /**
     * Checks that the file is not empty
     *
     * @param file file to be validated
     * @throws FileUploadException if the file is empty
     */
    private static void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Validation failed: File is empty");
            throw new FileUploadException(FileConstants.ERROR_EMPTY_FILE);
        }
    }

    /**
     * Checks that the file size is within the allowed limit
     *
     * @param file file to be validated
     * @throws FileUploadException if the file is too large
     */
    private static void validateFileSize(MultipartFile file) {
        if (file.getSize() > FileConstants.MAX_FILE_SIZE) {
            log.warn("Validation failed: File size exceeds limit. Size: {} bytes, Limit: {} bytes",
                    file.getSize(), FileConstants.MAX_FILE_SIZE);
            throw new FileUploadException(FileConstants.ERROR_FILE_TOO_LARGE);
        }
    }

    /**
     * Checks that the file type is one of the allowed formats
     *
     * @param file file to be validated
     * @throws FileUploadException if the file type is not allowed
     */
    private static void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !FileConstants.ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Validation failed: File format not allowed - {}", contentType);
            throw new FileUploadException(FileConstants.ERROR_INVALID_FORMAT);
        }

        log.debug("File type validated and approved: {}", contentType);
    }

    /**
     * Checks if the file extension is valid (additional security check)
     *
     * @param fileName file name
     * @return true - valid extension, false - invalid extension
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