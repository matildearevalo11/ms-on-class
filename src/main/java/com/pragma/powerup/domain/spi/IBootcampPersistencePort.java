package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Bootcamp;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface IBootcampPersistencePort {
    Mono<Bootcamp> save(Bootcamp bootcamp);
    Mono<Boolean> existsById(Long id);
    Flux<Long> findExclusiveCapacityIds(Long bootcampId);
    Mono<Void> deleteById(Long id);
}
