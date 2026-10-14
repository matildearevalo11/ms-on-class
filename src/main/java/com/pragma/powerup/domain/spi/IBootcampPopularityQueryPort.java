package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.MostEnrolledBootcamp;
import reactor.core.publisher.Mono;

public interface IBootcampPopularityQueryPort {
    Mono<MostEnrolledBootcamp> findMostEnrolled();
}
