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
    @Test
    void deletesOnlyCapacitiesExclusiveToTheBootcamp() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        when(persistence.existsById(7L)).thenReturn(Mono.just(true));
        when(persistence.findExclusiveCapacityIds(7L)).thenReturn(Flux.just(1L));
        when(capacities.deleteCapacities(List.of(1L))).thenReturn(Mono.empty());
        when(persistence.deleteById(7L)).thenReturn(Mono.empty());

        StepVerifier.create(new BootcampUseCase(persistence, capacities, mock(IReportOutboxPort.class)).deleteBootcamp(7L))
                .verifyComplete();
        var order = inOrder(persistence, capacities);
        order.verify(persistence).deleteById(7L);
        order.verify(capacities).deleteCapacities(List.of(1L));
    }

    @Test
    void deletesBootcampWithoutCallingCapacityWhenAllCapacitiesAreShared() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        when(persistence.existsById(7L)).thenReturn(Mono.just(true));
        when(persistence.findExclusiveCapacityIds(7L)).thenReturn(Flux.empty());
        when(persistence.deleteById(7L)).thenReturn(Mono.empty());

        StepVerifier.create(new BootcampUseCase(persistence, capacities, mock(IReportOutboxPort.class)).deleteBootcamp(7L))
                .verifyComplete();
        verifyNoInteractions(capacities);
        verify(persistence).deleteById(7L);
    }

    @Test
    void propagatesCapacityCleanupFailureSoTheLocalDeletionIsRolledBack() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        when(persistence.existsById(7L)).thenReturn(Mono.just(true));
        when(persistence.findExclusiveCapacityIds(7L)).thenReturn(Flux.just(1L));
        when(persistence.deleteById(7L)).thenReturn(Mono.empty());
        when(capacities.deleteCapacities(List.of(1L)))
                .thenReturn(Mono.error(new IllegalStateException("Unavailable")));

        StepVerifier.create(new BootcampUseCase(persistence, capacities, mock(IReportOutboxPort.class)).deleteBootcamp(7L))
                .expectError(IllegalStateException.class).verify();
        var order = inOrder(persistence, capacities);
        order.verify(persistence).deleteById(7L);
        order.verify(capacities).deleteCapacities(List.of(1L));
    }

    @Test
    void doesNotCallCapacityServiceWhenLocalDeletionFails() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        when(persistence.existsById(7L)).thenReturn(Mono.just(true));
        when(persistence.findExclusiveCapacityIds(7L)).thenReturn(Flux.just(1L));
        when(persistence.deleteById(7L)).thenReturn(Mono.error(new IllegalStateException("Database down")));

        StepVerifier.create(new BootcampUseCase(persistence, capacities, mock(IReportOutboxPort.class)).deleteBootcamp(7L))
                .expectError(IllegalStateException.class).verify();
        verifyNoInteractions(capacities);
    }

    @Test
    void reportsMissingBootcamp() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        when(persistence.existsById(7L)).thenReturn(Mono.just(false));

        StepVerifier.create(new BootcampUseCase(persistence, capacities, mock(IReportOutboxPort.class)).deleteBootcamp(7L))
                .expectError(NotFoundException.class).verify();
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
