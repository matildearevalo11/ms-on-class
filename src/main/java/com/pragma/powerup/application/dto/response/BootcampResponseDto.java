package com.pragma.powerup.application.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BootcampResponseDto {
    private final Long id;
    private final String name;
    private final String description;
    private final LocalDate launchDate;
    private final Integer durationDays;
    private final List<Long> capacityIds;
}
