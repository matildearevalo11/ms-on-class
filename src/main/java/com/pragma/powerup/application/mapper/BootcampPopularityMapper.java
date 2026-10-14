package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.CapacityDetailDto;
import com.pragma.powerup.application.dto.response.MostEnrolledBootcampResponseDto;
import com.pragma.powerup.application.dto.response.PersonSummaryDto;
import com.pragma.powerup.application.dto.response.TechnologySummaryDto;
import com.pragma.powerup.domain.model.MostEnrolledBootcampDetails;
import org.springframework.stereotype.Component;

@Component
public class BootcampPopularityMapper {
    public MostEnrolledBootcampResponseDto toResponse(MostEnrolledBootcampDetails details) {
        var bootcamp = details.getBootcamp();
        return new MostEnrolledBootcampResponseDto(bootcamp.getId(), bootcamp.getName(),
                bootcamp.getDescription(), bootcamp.getLaunchDate(), bootcamp.getDurationDays(),
                details.getPersonCount(), details.getPersons().stream()
                        .map(person -> new PersonSummaryDto(person.getId(), person.getName(), person.getEmail()))
                        .toList(),
                details.getCapacities().stream().map(capacity -> new CapacityDetailDto(capacity.getId(),
                        capacity.getName(), capacity.getTechnologies().stream()
                                .map(technology -> new TechnologySummaryDto(technology.getId(), technology.getName()))
                                .toList())).toList());
    }
}
