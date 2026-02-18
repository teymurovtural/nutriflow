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
 * Utility class for file operations
 */
@Slf4j
public final class FileOperationUtil {

    private FileOperationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Generates a unique file name
     *
     * @param originalFileName original file name
     * @return new unique file name combined with UUID
     */
    public static String generateUniqueFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            log.warn("Original file name is empty, using default name");
            originalFileName = "file";
        }

        String uniqueFileName = UUID.randomUUID().toString()
                + FileConstants.FILE_NAME_SEPARATOR
                + originalFileName;

        log.debug("Unique file name generated: {}", uniqueFileName);
        return uniqueFileName;
    }

    /**
     * Checks if a directory exists, creates it if not
     *
     * @param directoryPath directory path
     * @throws FileStorageException if the directory cannot be created
     */
    public static void ensureDirectoryExists(String directoryPath) {
        Path path = Paths.get(directoryPath);

        if (!Files.exists(path)) {
            try {
                log.info(FileConstants.LOG_UPLOAD_DIR_CREATED, directoryPath);
                Files.createDirectories(path);
                log.debug("Directory created successfully: {}", directoryPath);
            } catch (IOException e) {
                log.error("Error while creating directory: {}", e.getMessage());
                throw new FileStorageException("Directory could not be created: " + directoryPath, e);
            }
        }
    }

    /**
     * Checks if a file path is valid
     *
     * @param filePath file path
     * @return true - valid path, false - invalid path
     */
    public static boolean isValidFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            path.normalize();
            return true;
        } catch (Exception e) {
            log.warn("Invalid file path: {}", filePath);
            return false;
        }
    }

    /**
     * Checks if a file exists
     *
     * @param filePath file path
     * @return true - file exists, false - file does not exist
     */
    public static boolean fileExists(String filePath) {
        if (!isValidFilePath(filePath)) {
            return false;
        }

        Path path = Paths.get(filePath);
        return Files.exists(path);
    }

    /**
     * Extracts the extension from a file name
     *
     * @param fileName file name
     * @return extension (e.g. .pdf, .jpg) or empty string
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
     * Converts a file path to a Path object
     *
     * @param filePath file path
     * @return Path object
     * @throws FileStorageException if the path is invalid
     */
    public static Path toPath(String filePath) {
        if (!isValidFilePath(filePath)) {
            log.error("Invalid file path: {}", filePath);
            throw new FileStorageException(FileConstants.ERROR_INVALID_FILE_PATH);
        }

        return Paths.get(filePath);
    }
}