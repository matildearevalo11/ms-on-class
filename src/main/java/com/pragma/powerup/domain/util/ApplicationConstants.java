package com.pragma.powerup.domain.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationConstants {
    MAX_ACTIVE_BOOTCAMPS(5);

    private final int value;
}
