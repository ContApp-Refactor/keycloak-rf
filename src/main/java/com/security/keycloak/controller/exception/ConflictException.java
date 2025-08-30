package com.security.keycloak.controller.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s ya existe con %s: '%s'", resourceName, fieldName, fieldValue));
    }
}