package com.nutriflow.constants;

import java.util.Arrays;
import java.util.List;

/**
 * Constant values for file operations
 */
public final class FileConstants {

    private FileConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Allowed MIME types
    public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    // Allowed file extensions (for UI)
    public static final List<String> ALLOWED_FILE_EXTENSIONS = Arrays.asList(
            ".pdf",
            ".jpg",
            ".jpeg",
            ".png"
    );

    // Maximum file size (10MB - in bytes)
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Error messages
    public static final String ERROR_EMPTY_FILE = "No file selected or file is empty!";
    public static final String ERROR_INVALID_FORMAT = "Only PDF, JPG, and PNG formats are allowed!";
    public static final String ERROR_FILE_TOO_LARGE = "File size cannot be larger than 10MB!";
    public static final String ERROR_FILE_SAVE_FAILED = "An error occurred while saving the file";
    public static final String ERROR_FILE_DELETE_FAILED = "An error occurred while deleting the file";
    public static final String ERROR_INVALID_FILE_PATH = "Invalid file path";

    // Log messages
    public static final String LOG_FILE_UPLOAD_STARTED = "File upload process started. File name: {}, Size: {} bytes";
    public static final String LOG_FILE_SAVED_SUCCESS = "File saved successfully. New name: {}";
    public static final String LOG_FILE_DELETED_SUCCESS = "File deleted successfully: {}";
    public static final String LOG_FILE_NOT_FOUND = "File to be deleted not found: {}";
    public static final String LOG_UPLOAD_DIR_CREATED = "Creating upload directory: {}";

    // File name separator
    public static final String FILE_NAME_SEPARATOR = "_";
}