package com.nutriflow.exceptions;

public class HealthProfileNotFoundException extends RuntimeException {

    public HealthProfileNotFoundException(String message) {
        super(message);
    }

}
