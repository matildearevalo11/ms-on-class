package com.pragma.powerup.infrastructure.exceptionhandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private Map<String, String> errors;

    public static ErrorResponse from(String message) {
        return new ErrorResponse(Map.of("message", message));
    }
}
