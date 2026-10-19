package com.pragma.powerup.domain.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationConstants {
    MAX_ACTIVE_BOOTCAMPS(5),
    MIN_CAPACITIES_PER_BOOTCAMP(1),
    MAX_CAPACITIES_PER_BOOTCAMP(4);

    private final int value;
}
