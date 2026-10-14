package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersonSummary {
    private final Long id;
    private final String name;
    private final String email;
}
