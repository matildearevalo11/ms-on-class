package com.pragma.powerup.application.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CapacityDetailDto {
    private final Long id;
    private final String name;
    private final List<TechnologySummaryDto> technologies;
}
