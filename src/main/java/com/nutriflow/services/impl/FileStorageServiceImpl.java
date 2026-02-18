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
 * Service implementation for file storage and management.
 *
 * This service is used for saving and deleting files on disk.
 * Supported formats: PDF, JPG, PNG
 * Maximum file size: 10MB
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Saves a file to disk.
     *
     * @param file the file to be saved
     * @return the full path of the saved file
     * @throws IOException if an error occurs while saving the file
     * @throws FileStorageException if a validation or storage error occurs
     */
    @Override
    public String saveFile(MultipartFile file) throws IOException {
        log.info(FileConstants.LOG_FILE_UPLOAD_STARTED,
                file.getOriginalFilename(), file.getSize());

        // 1. Validate the file
        FileValidationUtil.validateFile(file);

        // 2. Prepare the upload directory
        FileOperationUtil.ensureDirectoryExists(uploadDir);

        // 3. Generate a unique file name
        String uniqueFileName = FileOperationUtil.generateUniqueFileName(
                file.getOriginalFilename());

        // 4. Determine the full file path
        Path uploadPath = FileOperationUtil.toPath(uploadDir);
        Path filePath = uploadPath.resolve(uniqueFileName);

        // 5. Write the file to disk
        saveFileToDisk(file, filePath);

        log.info(FileConstants.LOG_FILE_SAVED_SUCCESS, uniqueFileName);
        return filePath.toString();
    }

    /**
     * Physically writes the file to disk.
     *
     * @param file the file to be written
     * @param targetPath the target file path
     * @throws IOException if an error occurs during the write operation
     */
    private void saveFileToDisk(MultipartFile file, Path targetPath) throws IOException {
        try {
            log.debug("Writing file to disk: {}", targetPath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error occurred while writing file to disk: {}", e.getMessage(), e);
            throw new FileStorageException(FileConstants.ERROR_FILE_SAVE_FAILED, e);
        }
    }

    /**
     * Deletes a file from disk.
     *
     * @param filePath the path of the file to be deleted
     * @throws IOException if an error occurs while deleting the file
     * @throws FileStorageException if the path is invalid
     */
    @Override
    public void deleteFile(String filePath) throws IOException {
        log.info("File deletion request: {}", filePath);

        // Validate the file path
        if (!FileOperationUtil.isValidFilePath(filePath)) {
            log.error("Invalid file path: {}", filePath);
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
            log.error("Error occurred while deleting file: {}", e.getMessage(), e);
            throw new FileStorageException(FileConstants.ERROR_FILE_DELETE_FAILED, e);
        }
    }
}