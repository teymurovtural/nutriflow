package com.nutriflow.exceptions;

/**
 * Fayl storage əməliyyatlarında baş verən xətalar üçün exception
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStorageException(Throwable cause) {
        super(cause);
    }
}