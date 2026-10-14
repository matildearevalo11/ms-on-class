package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.BootcampPageQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampQueryPort {
    Mono<Long> count();
    Flux<Bootcamp> findPage(BootcampPageQuery query);
}
