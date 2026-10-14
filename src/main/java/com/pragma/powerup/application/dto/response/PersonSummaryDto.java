package com.pragma.powerup.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersonSummaryDto {
    private final Long id;
    private final String name;
    private final String email;
}
