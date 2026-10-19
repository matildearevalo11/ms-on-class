package com.pragma.powerup.domain.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BootcampCapacityUsage {
    private final List<Long> capacityIds;
    private final boolean sharesCapacities;
}
