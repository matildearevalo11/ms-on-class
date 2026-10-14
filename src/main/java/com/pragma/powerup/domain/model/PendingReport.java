package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingReport {
    private final Long id;
    private final String payload;
}
