package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ExceptionMessages;
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
        when(persistence.save(any(Bootcamp.class))).thenReturn(Mono.just(saved));
        when(outbox.enqueue(any())).thenReturn(Mono.empty());

        StepVerifier.create(new BootcampUseCase(persistence, capacities, outbox).createBootcamp(input))
                .expectNext(saved).verifyComplete();
        verify(persistence).save(argThat(bootcamp -> bootcamp.getName().equals("Backend")
                && bootcamp.getCapacityIds().equals(List.of(1L, 2L))));
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

    @Test
    void stripsTextBeforeSaving() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        var outbox = mock(IReportOutboxPort.class);
        when(capacities.findDetailsByIds(List.of(1L))).thenReturn(Mono.just(Map.of(
                1L, new CapacityDetail(1L, "First", List.of()))));
        when(persistence.save(any(Bootcamp.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(outbox.enqueue(any())).thenReturn(Mono.empty());

        StepVerifier.create(new BootcampUseCase(persistence, capacities, outbox).createBootcamp(
                        new Bootcamp(null, " Backend ", " Services ", LocalDate.of(2026, 11, 1), 90, List.of(1L))))
                .assertNext(saved -> {
                    org.assertj.core.api.Assertions.assertThat(saved.getName()).isEqualTo("Backend");
                    org.assertj.core.api.Assertions.assertThat(saved.getDescription()).isEqualTo("Services");
                }).verifyComplete();
    }

    @Test
    void rejectsInvalidBootcampsBeforeCallingOtherServices() {
        var date = LocalDate.of(2026, 11, 1);
        var invalid = Map.of(
                new Bootcamp(null, " ", "Services", date, 90, List.of(1L)), ExceptionMessages.BOOTCAMP_NAME_REQUIRED,
                new Bootcamp(null, "Backend", null, date, 90, List.of(1L)), ExceptionMessages.BOOTCAMP_DESCRIPTION_REQUIRED,
                new Bootcamp(null, "Backend", "Services", null, 90, List.of(1L)), ExceptionMessages.LAUNCH_DATE_REQUIRED,
                new Bootcamp(null, "Backend", "Services", date, 0, List.of(1L)), ExceptionMessages.DURATION_INVALID,
                new Bootcamp(null, "Backend", "Services", date, 90, List.of()), ExceptionMessages.CAPACITY_COUNT_OUT_OF_RANGE,
                new Bootcamp(null, "Backend", "Services", date, 90, List.of(1L, 2L, 3L, 4L, 5L)),
                ExceptionMessages.CAPACITY_COUNT_OUT_OF_RANGE,
                new Bootcamp(null, "Backend", "Services", date, 90, List.of(0L)), ExceptionMessages.CAPACITY_ID_INVALID,
                new Bootcamp(null, "Backend", "Services", date, 90, List.of(1L, 1L)), ExceptionMessages.CAPACITIES_REPEATED);
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        var outbox = mock(IReportOutboxPort.class);

        invalid.forEach((bootcamp, message) -> StepVerifier.create(
                        new BootcampUseCase(persistence, capacities, outbox).createBootcamp(bootcamp))
                .expectErrorSatisfies(error -> org.assertj.core.api.Assertions.assertThat(error)
                        .isInstanceOf(ValidationException.class).hasMessage(message.getMessage()))
                .verify());
        verifyNoInteractions(persistence, capacities, outbox);
    }
}
