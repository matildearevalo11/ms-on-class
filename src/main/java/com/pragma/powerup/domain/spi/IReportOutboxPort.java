package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.BootcampReportSnapshot;
import com.pragma.powerup.domain.model.PendingPersonCount;
import com.pragma.powerup.domain.model.PendingReport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IReportOutboxPort {
    Mono<Void> enqueue(BootcampReportSnapshot snapshot);
    Flux<PendingReport> findPending(int limit);
    Mono<Void> markDelivered(Long id);
    Mono<Void> discardPending(Long bootcampId);
    Mono<Void> enqueuePersonCountChange(Long bootcampId);
    Flux<PendingPersonCount> findPendingPersonCounts(int limit);
    Mono<Void> markPersonCountDelivered(PendingPersonCount event);
}
