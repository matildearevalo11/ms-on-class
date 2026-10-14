package com.pragma.powerup.domain.api;

import reactor.core.publisher.Mono;

public interface IEnrollmentServicePort {
    Mono<Void> enroll(Long personId, Long bootcampId);
}
