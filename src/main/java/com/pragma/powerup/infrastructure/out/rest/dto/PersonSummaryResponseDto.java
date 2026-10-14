package com.pragma.powerup.infrastructure.out.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonSummaryResponseDto {
    private Long id;
    private String name;
    private String email;
}
