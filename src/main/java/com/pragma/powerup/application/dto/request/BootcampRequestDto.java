package com.pragma.powerup.application.dto.request;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BootcampRequestDto {
    private String name;
    private String description;
    private LocalDate launchDate;
    private Integer durationDays;
    private List<Long> capacityIds;
}
