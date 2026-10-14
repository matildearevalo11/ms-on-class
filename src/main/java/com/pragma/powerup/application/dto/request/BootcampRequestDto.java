package com.pragma.powerup.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BootcampRequestDto {
    @NotBlank(message = "Bootcamp name is required.")
    private String name;

    @NotBlank(message = "Bootcamp description is required.")
    private String description;

    @NotNull(message = "Launch date is required.")
    private LocalDate launchDate;

    @NotNull(message = "Duration is required.")
    @Positive(message = "Duration must be greater than zero.")
    private Integer durationDays;

    @NotNull(message = "Capacity IDs are required.")
    @Size(min = 1, max = 4, message = "A bootcamp must have between 1 and 4 capacities.")
    private List<@NotNull(message = "Capacity ID is required.")
            @Positive(message = "Capacity ID must be positive.") Long> capacityIds;

    @JsonIgnore
    @AssertTrue(message = "Capacities must not be repeated.")
    public boolean isCapacityIdsUnique() {
        return capacityIds == null || new HashSet<>(capacityIds).size() == capacityIds.size();
    }
}
