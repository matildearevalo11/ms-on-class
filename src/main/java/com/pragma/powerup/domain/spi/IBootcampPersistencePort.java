package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Bootcamp;
import reactor.core.publisher.Mono;

public interface IBootcampPersistencePort {
    Mono<Bootcamp> save(Bootcamp bootcamp);
}
