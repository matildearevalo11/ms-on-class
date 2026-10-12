package com.pragma.powerup.domain.spi;

import java.util.List;
import reactor.core.publisher.Mono;

public interface ICapacityQueryPort {
    Mono<List<Long>> findExistingIds(List<Long> ids);
}
