package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Bootcamp;
import java.util.List;
import reactor.core.publisher.Mono;

public interface IBootcampPersistencePort {
    Mono<Bootcamp> save(Bootcamp bootcamp);
    Mono<List<Long>> findExclusiveCapacityIds(Long bootcampId);
    Mono<Void> deleteById(Long id);
}
