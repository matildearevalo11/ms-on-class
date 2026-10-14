package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.Bootcamp;
import reactor.core.publisher.Mono;

public interface IBootcampServicePort {
    Mono<Bootcamp> createBootcamp(Bootcamp bootcamp);
}
