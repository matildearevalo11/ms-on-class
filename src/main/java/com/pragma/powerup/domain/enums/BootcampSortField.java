package com.pragma.powerup.domain.enums;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BootcampSortField {
    NAME("name"),
    CAPACITY_COUNT("capacityCount");

    private final String value;

    public static BootcampSortField fromValue(String value) {
        return Arrays.stream(values())
                .filter(field -> field.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new ValidationException(ExceptionMessages.INVALID_SORT_FIELD.getMessage()));
    }
}
