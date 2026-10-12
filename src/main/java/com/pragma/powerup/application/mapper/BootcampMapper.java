package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.request.BootcampRequestDto;
import com.pragma.powerup.application.dto.response.BootcampResponseDto;
import com.pragma.powerup.domain.model.Bootcamp;
import org.springframework.stereotype.Component;

@Component
public class BootcampMapper {
    public Bootcamp toModel(BootcampRequestDto request) {
        return new Bootcamp(null, request.getName().strip(), request.getDescription().strip(),
                request.getLaunchDate(), request.getDurationDays(), request.getCapacityIds());
    }

    public BootcampResponseDto toResponse(Bootcamp bootcamp) {
        return new BootcampResponseDto(bootcamp.getId(), bootcamp.getName(), bootcamp.getDescription(),
                bootcamp.getLaunchDate(), bootcamp.getDurationDays(), bootcamp.getCapacityIds());
    }
}
