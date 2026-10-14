package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.model.PendingReport;
import com.pragma.powerup.domain.spi.IReportDeliveryPort;
import com.pragma.powerup.domain.spi.IReportOutboxPort;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
}
