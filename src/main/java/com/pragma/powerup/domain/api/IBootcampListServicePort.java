package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.BootcampPage;
import com.pragma.powerup.domain.model.BootcampPageQuery;
import reactor.core.publisher.Mono;

public interface IBootcampListServicePort {
    Mono<BootcampPage> list(BootcampPageQuery query);
}
