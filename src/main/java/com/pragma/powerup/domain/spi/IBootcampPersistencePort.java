package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.BootcampCapacityUsage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampPersistencePort {
    Mono<Bootcamp> save(Bootcamp bootcamp);
    Mono<BootcampCapacityUsage> findCapacityUsage(Long bootcampId);
    Flux<Long> findCapacityIdsOfOtherBootcamps(Long bootcampId);
    Mono<Void> deleteById(Long id);
}
