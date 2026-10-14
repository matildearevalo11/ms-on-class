package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.request.BootcampPageRequestDto;
import com.pragma.powerup.application.dto.response.BootcampListItemDto;
import com.pragma.powerup.application.dto.response.BootcampPageResponseDto;
import com.pragma.powerup.application.dto.response.CapacityDetailDto;
import com.pragma.powerup.application.dto.response.TechnologySummaryDto;
import com.pragma.powerup.domain.model.BootcampPage;
import com.pragma.powerup.domain.model.BootcampPageQuery;
import org.springframework.stereotype.Component;

@Component
public class BootcampPageMapper {
    public BootcampPageQuery toQuery(BootcampPageRequestDto request) {
        return new BootcampPageQuery(request.getPage(), request.getSize(), request.getSortBy(),
                "desc".equals(request.getDirection()));
    }

    public BootcampPageResponseDto toResponse(BootcampPage page) {
        var content = page.getContent().stream().map(bootcamp -> new BootcampListItemDto(
                bootcamp.getId(), bootcamp.getName(), bootcamp.getDescription(),
                bootcamp.getLaunchDate(), bootcamp.getDurationDays(),
                bootcamp.getCapacities().stream().map(capacity -> new CapacityDetailDto(
                        capacity.getId(), capacity.getName(), capacity.getTechnologies().stream()
                                .map(technology -> new TechnologySummaryDto(technology.getId(), technology.getName()))
                                .toList())).toList())).toList();
        return new BootcampPageResponseDto(content, page.getPage(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
