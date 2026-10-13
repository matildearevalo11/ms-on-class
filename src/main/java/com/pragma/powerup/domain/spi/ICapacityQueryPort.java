package com.pragma.powerup.domain.spi;

import java.util.List;
import java.util.Map;
import com.pragma.powerup.domain.model.CapacityDetail;
import reactor.core.publisher.Mono;

public interface ICapacityQueryPort {
    Mono<List<Long>> findExistingIds(List<Long> ids);
    Mono<Map<Long, CapacityDetail>> findDetailsByIds(List<Long> ids);
}
