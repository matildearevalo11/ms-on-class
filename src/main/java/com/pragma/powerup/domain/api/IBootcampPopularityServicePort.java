package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.MostEnrolledBootcampDetails;
import reactor.core.publisher.Mono;

public interface IBootcampPopularityServicePort {
    Mono<MostEnrolledBootcampDetails> findMostEnrolled();
}
