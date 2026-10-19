package com.pragma.powerup.domain.spi;

import reactor.core.publisher.Mono;

public interface IReportDeliveryPort {
    Mono<Void> deliver(String payload);
    Mono<Void> deliverPersonCount(Long bootcampId, long personCount);
}
