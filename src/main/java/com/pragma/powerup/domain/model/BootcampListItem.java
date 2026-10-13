package com.pragma.powerup.domain.model;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BootcampListItem {
    private final Long id;
    private final String name;
    private final String description;
    private final LocalDate launchDate;
    private final Integer durationDays;
    private final List<CapacityDetail> capacities;
}
