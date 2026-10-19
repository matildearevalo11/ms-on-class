package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingPersonCount {
    private final Long eventId;
    private final Long bootcampId;
    private final long personCount;
}
