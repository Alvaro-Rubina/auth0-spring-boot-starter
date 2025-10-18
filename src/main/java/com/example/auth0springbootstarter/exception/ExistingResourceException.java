package com.example.auth0springbootstarter.exception;

public class ExistingResourceException extends RuntimeException {
    public ExistingResourceException(String message) {
        super(message);
    }
}
