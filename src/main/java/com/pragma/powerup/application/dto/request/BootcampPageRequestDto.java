package com.pragma.powerup.application.dto.request;

import com.pragma.powerup.domain.enums.BootcampSortField;
import com.pragma.powerup.domain.enums.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BootcampPageRequestDto {
    @Min(value = 0, message = ValidationMessages.PAGE_MIN)
    private int page = 0;

    @Min(value = 1, message = ValidationMessages.SIZE_RANGE)
    @Max(value = 100, message = ValidationMessages.SIZE_RANGE)
    private int size = 10;

    private String sortBy = BootcampSortField.NAME.getValue();

    private String direction = SortDirection.ASC.getValue();
}
