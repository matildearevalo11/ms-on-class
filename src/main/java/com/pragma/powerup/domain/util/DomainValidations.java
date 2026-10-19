package com.pragma.powerup.domain.util;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import java.util.HashSet;
import java.util.List;

public final class DomainValidations {
    private DomainValidations() {
    }

    public static <T> T requirePresent(T value, ExceptionMessages message) {
        if (value == null) {
            throw invalid(message);
        }
        return value;
    }

    public static String requireText(String value, ExceptionMessages message) {
        if (value == null || value.isBlank()) {
            throw invalid(message);
        }
        return value.strip();
    }

    public static String requireMaxLength(String value, int maxLength, ExceptionMessages message) {
        if (value.codePointCount(0, value.length()) > maxLength) {
            throw invalid(message);
        }
        return value;
    }

    public static <N extends Number> N requirePositive(N value, ExceptionMessages message) {
        if (value == null || value.longValue() <= 0) {
            throw invalid(message);
        }
        return value;
    }

    public static <N extends Number> N requireNotNegative(N value, ExceptionMessages message) {
        if (value == null || value.longValue() < 0) {
            throw invalid(message);
        }
        return value;
    }

    public static <T> List<T> requireSizeBetween(List<T> values, int min, int max, ExceptionMessages message) {
        if (values == null || values.size() < min || values.size() > max) {
            throw invalid(message);
        }
        return values;
    }

    public static List<Long> requirePositiveIds(List<Long> ids, ExceptionMessages message) {
        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw invalid(message);
        }
        return ids;
    }

    public static <T> List<T> requireUnique(List<T> values, ExceptionMessages message) {
        if (new HashSet<>(values).size() != values.size()) {
            throw invalid(message);
        }
        return values;
    }

    private static ValidationException invalid(ExceptionMessages message) {
        return new ValidationException(message.getMessage());
    }
}
