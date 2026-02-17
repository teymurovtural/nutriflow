package com.nutriflow.constants;

import java.util.Arrays;
import java.util.List;

/**
 * File əməliyyatları üçün constant dəyərlər
 */
public final class FileConstants {

    private FileConstants() {
        throw new UnsupportedOperationException("Bu utility class-dır, instantiate edilə bilməz");
    }

    // Icazə verilən MIME type-lar
    public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    // Icazə verilən fayl extension-ları (UI üçün)
    public static final List<String> ALLOWED_FILE_EXTENSIONS = Arrays.asList(
            ".pdf",
            ".jpg",
            ".jpeg",
            ".png"
    );

    // Maksimum fayl ölçüsü (10MB - byte ilə)
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Error message-lər
    public static final String ERROR_EMPTY_FILE = "Fayl seçilməyib və ya boşdur!";
    public static final String ERROR_INVALID_FORMAT = "Yalnız PDF, JPG və PNG formatlarına icazə verilir!";
    public static final String ERROR_FILE_TOO_LARGE = "Fayl ölçüsü 10MB-dan böyük ola bilməz!";
    public static final String ERROR_FILE_SAVE_FAILED = "Fayl saxlanılarkən xəta baş verdi";
    public static final String ERROR_FILE_DELETE_FAILED = "Fayl silinərkən xəta baş verdi";
    public static final String ERROR_INVALID_FILE_PATH = "Fayl yolu düzgün deyil";

    // Log message-lər
    public static final String LOG_FILE_UPLOAD_STARTED = "Fayl yükləmə prosesi başladı. Fayl adı: {}, Ölçü: {} bytes";
    public static final String LOG_FILE_SAVED_SUCCESS = "Fayl uğurla saxlanıldı. Yeni ad: {}";
    public static final String LOG_FILE_DELETED_SUCCESS = "Fayl uğurla silindi: {}";
    public static final String LOG_FILE_NOT_FOUND = "Silinməli olan fayl tapılmadı: {}";
    public static final String LOG_UPLOAD_DIR_CREATED = "Yükləmə qovluğu yaradılır: {}";

    // Fayl adı separator
    public static final String FILE_NAME_SEPARATOR = "_";
}