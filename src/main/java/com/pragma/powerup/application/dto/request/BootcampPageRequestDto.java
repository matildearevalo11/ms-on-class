package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BootcampPageRequestDto {
    @Min(value = 0, message = "Page must be zero or greater.")
    private int page = 0;

    @Min(value = 1, message = "Size must be between 1 and 100.")
    @Max(value = 100, message = "Size must be between 1 and 100.")
    private int size = 10;

    @Pattern(regexp = "name|capacityCount", message = "Sort by name or capacityCount.")
    @NotBlank(message = "Sort field is required.")
    private String sortBy = "name";

    @Pattern(regexp = "asc|desc", message = "Direction must be asc or desc.")
    @NotBlank(message = "Direction is required.")
    private String direction = "asc";
}
