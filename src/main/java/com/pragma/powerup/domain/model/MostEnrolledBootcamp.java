package com.pragma.powerup.domain.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MostEnrolledBootcamp {
    private final Bootcamp bootcamp;
    private final List<Long> personIds;
}
