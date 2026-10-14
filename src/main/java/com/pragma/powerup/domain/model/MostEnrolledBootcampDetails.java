package com.pragma.powerup.domain.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MostEnrolledBootcampDetails {
    private final Bootcamp bootcamp;
    private final List<PersonSummary> persons;
    private final List<CapacityDetail> capacities;

    public int getPersonCount() {
        return persons.size();
    }
}
