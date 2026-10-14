package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.PersonSummary;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

public interface IPersonQueryPort {
    Mono<Map<Long, PersonSummary>> findByIds(List<Long> ids);
}
