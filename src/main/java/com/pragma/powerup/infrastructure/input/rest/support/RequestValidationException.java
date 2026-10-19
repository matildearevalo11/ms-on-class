package com.pragma.powerup.infrastructure.input.rest.support;

public class RequestValidationException extends RuntimeException {
    public RequestValidationException(String message) {
        super(message);
    }
}
