package com.pragma.powerup.infrastructure.out.r2dbc.entity;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@AllArgsConstructor
@Table("bootcamps")
public class BootcampEntity {
    @Id
    private final Long id;
    private final String name;
    private final String description;
    private final LocalDate launchDate;
    private final Integer durationDays;
}
