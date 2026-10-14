package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.response.MostEnrolledBootcampResponseDto;
import reactor.core.publisher.Mono;

public interface IBootcampPopularityHandler {
    Mono<MostEnrolledBootcampResponseDto> findMostEnrolled();
}
