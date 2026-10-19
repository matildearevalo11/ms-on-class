package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.exception.NotFoundException;
import reactor.core.publisher.Flux;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.spi.IBootcampPersistencePort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import com.pragma.powerup.domain.spi.IReportOutboxPort;
import com.pragma.powerup.domain.model.CapacityDetail;
import com.pragma.powerup.domain.model.TechnologySummary;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;

class BootcampUseCaseTest {
    private final IBootcampPersistencePort persistence = mock(IBootcampPersistencePort.class);
    private final ICapacityQueryPort capacities = mock(ICapacityQueryPort.class);
    private final IReportOutboxPort outbox = mock(IReportOutboxPort.class);

    private Mono<Void> delete() {
        when(outbox.discardPending(7L)).thenReturn(Mono.empty());
        return new BootcampUseCase(persistence, capacities, outbox).deleteBootcamp(7L);
    }

    @Test
    void deletesLocallyDiscardsPendingReportAndThenExclusiveCapacities() {
        when(persistence.findExclusiveCapacityIds(7L)).thenReturn(Mono.just(List.of(1L)));
        when(persistence.deleteById(7L)).thenReturn(Mono.empty());
        when(capacities.deleteCapacities(List.of(1L))).thenReturn(Mono.empty());

        StepVerifier.create(delete()).verifyComplete();
        var order = inOrder(persistence, outbox, capacities);
        order.verify(persistence).deleteById(7L);
        order.verify(outbox).discardPending(7L);
        order.verify(capacities).deleteCapacities(List.of(1L));
    }

    @Test
    void deletesBootcampWithoutCallingCapacityWhenAllCapacitiesAreShared() {
        when(persistence.findExclusiveCapacityIds(7L)).thenReturn(Mono.just(List.of()));
        when(persistence.deleteById(7L)).thenReturn(Mono.empty());

        StepVerifier.create(delete()).verifyComplete();
        verifyNoInteractions(capacities);
        verify(persistence).deleteById(7L);
    }

    @Test
    void propagatesCapacityCleanupFailureSoTheLocalDeletionIsRolledBack() {
        when(persistence.findExclusiveCapacityIds(7L)).thenReturn(Mono.just(List.of(1L)));
        when(persistence.deleteById(7L)).thenReturn(Mono.empty());
        when(capacities.deleteCapacities(List.of(1L)))
                .thenReturn(Mono.error(new IllegalStateException("Unavailable")));

        StepVerifier.create(delete()).expectError(IllegalStateException.class).verify();
        var order = inOrder(persistence, capacities);
        order.verify(persistence).deleteById(7L);
        order.verify(capacities).deleteCapacities(List.of(1L));
    }

    @Test
    void doesNotCallCapacityServiceWhenLocalDeletionFails() {
        when(persistence.findExclusiveCapacityIds(7L)).thenReturn(Mono.just(List.of(1L)));
        when(persistence.deleteById(7L)).thenReturn(Mono.error(new IllegalStateException("Database down")));

        StepVerifier.create(delete()).expectError(IllegalStateException.class).verify();
        verifyNoInteractions(capacities);
        verify(outbox, never()).discardPending(7L);
    }

    @Test
    void reportsMissingBootcampWithoutDeletingAnything() {
        when(persistence.findExclusiveCapacityIds(7L)).thenReturn(Mono.empty());

        StepVerifier.create(delete()).expectError(NotFoundException.class).verify();
        verify(persistence, never()).deleteById(7L);
        verifyNoInteractions(capacities);
    }

    @Test
    void savesOnlyWhenEveryCapacityExists() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        var outbox = mock(IReportOutboxPort.class);
        var input = new Bootcamp(null, "Backend", "Services", LocalDate.of(2026, 11, 1), 90, List.of(1L, 2L));
        var saved = new Bootcamp(7L, input.getName(), input.getDescription(), input.getLaunchDate(),
                input.getDurationDays(), input.getCapacityIds());
        when(capacities.findDetailsByIds(input.getCapacityIds())).thenReturn(Mono.just(Map.of(
                1L, new CapacityDetail(1L, "First", List.of(new TechnologySummary(3L, "Java"))),
                2L, new CapacityDetail(2L, "Second", List.of(new TechnologySummary(3L, "Java"))))));
        when(persistence.save(input)).thenReturn(Mono.just(saved));
        when(outbox.enqueue(any())).thenReturn(Mono.empty());

        StepVerifier.create(new BootcampUseCase(persistence, capacities, outbox).createBootcamp(input))
                .expectNext(saved).verifyComplete();
        verify(persistence).save(input);
        verify(outbox).enqueue(argThat(report -> report.getCapacityCount() == 2
                && report.getTechnologyCount() == 1 && report.getPersonCount() == 0));
    }

    @Test
    void refusesToSaveWhenACapacityIsMissing() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        var outbox = mock(IReportOutboxPort.class);
        var input = new Bootcamp(null, "Backend", "Services", LocalDate.of(2026, 11, 1), 90, List.of(1L, 2L));
        when(capacities.findDetailsByIds(input.getCapacityIds())).thenReturn(Mono.just(Map.of(
                1L, new CapacityDetail(1L, "First", List.of()))));

        StepVerifier.create(new BootcampUseCase(persistence, capacities, outbox).createBootcamp(input))
                .expectError(ValidationException.class).verify();
        verifyNoInteractions(persistence);
        verifyNoInteractions(outbox);
    }
}
