package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.model.PendingReport;
import com.pragma.powerup.domain.spi.IReportDeliveryPort;
import com.pragma.powerup.domain.spi.IReportOutboxPort;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportDeliveryUseCaseTest {
    @Test
    void leavesFailedDeliveryPendingForRetry() {
        IReportOutboxPort outbox = mock(IReportOutboxPort.class);
        IReportDeliveryPort delivery = mock(IReportDeliveryPort.class);
        when(outbox.findPending(10)).thenReturn(Flux.just(new PendingReport(1L, "{}")));
        when(delivery.deliver("{}")).thenReturn(Mono.error(new IllegalStateException("Unavailable")));

        StepVerifier.create(new ReportDeliveryUseCase(outbox, delivery).deliverPending(10))
                .expectError(IllegalStateException.class).verify();

        verify(outbox, never()).markDelivered(1L);
    }

    @Test
    void deliversReportsBeforePersonCountsAndMarksBoth() {
        IReportOutboxPort outbox = mock(IReportOutboxPort.class);
        IReportDeliveryPort delivery = mock(IReportDeliveryPort.class);
        var count = new com.pragma.powerup.domain.model.PendingPersonCount(5L, 7L, 3L);
        when(outbox.findPending(10)).thenReturn(Flux.just(new PendingReport(1L, "{}")));
        when(delivery.deliver("{}")).thenReturn(Mono.empty());
        when(outbox.markDelivered(1L)).thenReturn(Mono.empty());
        when(outbox.findPendingPersonCounts(10)).thenReturn(Flux.just(count));
        when(delivery.deliverPersonCount(7L, 3L)).thenReturn(Mono.empty());
        when(outbox.markPersonCountDelivered(count)).thenReturn(Mono.empty());

        StepVerifier.create(new ReportDeliveryUseCase(outbox, delivery).deliverPending(10)).verifyComplete();
        var order = inOrder(delivery, outbox);
        order.verify(delivery).deliver("{}");
        order.verify(outbox).markDelivered(1L);
        order.verify(delivery).deliverPersonCount(7L, 3L);
        order.verify(outbox).markPersonCountDelivered(count);
    }

    @Test
    void keepsPersonCountPendingWhenReportServiceRejectsIt() {
        IReportOutboxPort outbox = mock(IReportOutboxPort.class);
        IReportDeliveryPort delivery = mock(IReportDeliveryPort.class);
        var count = new com.pragma.powerup.domain.model.PendingPersonCount(5L, 7L, 3L);
        when(outbox.findPending(10)).thenReturn(Flux.empty());
        when(outbox.findPendingPersonCounts(10)).thenReturn(Flux.just(count));
        when(delivery.deliverPersonCount(7L, 3L)).thenReturn(Mono.error(new IllegalStateException("Not found")));

        StepVerifier.create(new ReportDeliveryUseCase(outbox, delivery).deliverPending(10))
                .expectError(IllegalStateException.class).verify();
        verify(outbox, never()).markPersonCountDelivered(count);
    }
}
