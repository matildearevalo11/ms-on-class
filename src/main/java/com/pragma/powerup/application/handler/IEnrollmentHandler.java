package com.pragma.powerup.application.handler;

import reactor.core.publisher.Mono;

public interface IEnrollmentHandler {
    Mono<Void> enroll(Long personId, Long bootcampId);
}
