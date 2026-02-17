package com.nutriflow.security;

/**
 * JWT token-i invalid olduqda throw edilən exception
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}