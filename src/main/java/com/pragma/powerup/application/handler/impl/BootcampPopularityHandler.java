package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.response.MostEnrolledBootcampResponseDto;
import com.pragma.powerup.application.handler.IBootcampPopularityHandler;
import com.pragma.powerup.application.mapper.BootcampPopularityMapper;
import com.pragma.powerup.domain.api.IBootcampPopularityServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BootcampPopularityHandler implements IBootcampPopularityHandler {
    private final IBootcampPopularityServicePort service;
    private final BootcampPopularityMapper mapper;

    @Override
    public Mono<MostEnrolledBootcampResponseDto> findMostEnrolled() {
        return service.findMostEnrolled().map(mapper::toResponse);
    }
}
