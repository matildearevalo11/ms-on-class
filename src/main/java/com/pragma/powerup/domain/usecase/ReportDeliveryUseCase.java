package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IReportDeliveryServicePort;
import com.pragma.powerup.domain.spi.IReportDeliveryPort;
import com.pragma.powerup.domain.spi.IReportOutboxPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReportDeliveryUseCase implements IReportDeliveryServicePort {
    private final IReportOutboxPort outbox;
    private final IReportDeliveryPort delivery;

    @Override
    public Mono<Void> deliverPending(int limit) {
        return deliverReports(limit).then(Mono.defer(() -> deliverPersonCounts(limit)));
    }

    private Mono<Void> deliverReports(int limit) {
        return outbox.findPending(limit)
                .concatMap(event -> delivery.deliver(event.getPayload())
                        .then(Mono.defer(() -> outbox.markDelivered(event.getId()))))
                .then();
    }

    private Mono<Void> deliverPersonCounts(int limit) {
        return outbox.findPendingPersonCounts(limit)
                .concatMap(event -> delivery.deliverPersonCount(event.getBootcampId(), event.getPersonCount())
                        .then(Mono.defer(() -> outbox.markPersonCountDelivered(event))))
                .then();
    }
}
