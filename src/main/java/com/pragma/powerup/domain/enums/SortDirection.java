package com.pragma.powerup.domain.enums;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortDirection {
    ASC("asc"),
    DESC("desc");

    private final String value;

    public static SortDirection fromValue(String value) {
        return Arrays.stream(values())
                .filter(direction -> direction.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new ValidationException(ExceptionMessages.INVALID_SORT_DIRECTION.getMessage()));
    }
}
