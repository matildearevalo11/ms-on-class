package com.pragma.powerup.domain.api;

import reactor.core.publisher.Mono;

public interface IReportDeliveryServicePort {
    Mono<Void> deliverPending(int limit);
}
