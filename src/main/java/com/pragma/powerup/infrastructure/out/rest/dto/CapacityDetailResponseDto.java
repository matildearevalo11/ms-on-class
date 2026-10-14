package com.pragma.powerup.infrastructure.out.rest.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CapacityDetailResponseDto {
    private Long id;
    private String name;
    private List<TechnologySummaryResponseDto> technologies;
}
