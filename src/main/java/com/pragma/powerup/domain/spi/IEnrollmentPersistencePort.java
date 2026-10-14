package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Bootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IEnrollmentPersistencePort {
    Mono<Bootcamp> findBootcamp(Long bootcampId);
    Flux<Bootcamp> findEnrolledBootcamps(Long personId);
    Mono<Void> save(Long personId, Long bootcampId);
}
