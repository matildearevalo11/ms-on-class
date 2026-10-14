package com.pragma.powerup.domain.model;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class BootcampReportSnapshot {
    private final Long bootcampId;
    private final String name;
    private final String description;
    private final LocalDate launchDate;
    private final Integer durationDays;
    private final List<CapacityDetail> capacities;
    private final int capacityCount;
    private final long technologyCount;
    private final int personCount;

    public BootcampReportSnapshot(Bootcamp bootcamp, List<CapacityDetail> capacities) {
        this.bootcampId = bootcamp.getId();
        this.name = bootcamp.getName();
        this.description = bootcamp.getDescription();
        this.launchDate = bootcamp.getLaunchDate();
        this.durationDays = bootcamp.getDurationDays();
        this.capacities = List.copyOf(capacities);
        this.capacityCount = capacities.size();
        this.technologyCount = capacities.stream().flatMap(capacity -> capacity.getTechnologies().stream())
                .map(TechnologySummary::getId).distinct().count();
        this.personCount = 0;
    }
}
