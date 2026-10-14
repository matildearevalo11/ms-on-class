package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ConflictException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.spi.IEnrollmentPersistencePort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentUseCaseTest {
    @Mock private IEnrollmentPersistencePort persistence;
    private EnrollmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EnrollmentUseCase(persistence,
                Clock.fixed(Instant.parse("2026-10-13T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void allowsAdjacentSchedulesAndPersistsEnrollment() {
        Bootcamp requested = bootcamp(2L, "2026-11-06", 5);
        when(persistence.findBootcamp(2L)).thenReturn(Mono.just(requested));
        when(persistence.findEnrolledBootcamps(1L)).thenReturn(Flux.just(bootcamp(1L, "2026-11-01", 5)));
        when(persistence.save(1L, 2L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.enroll(1L, 2L)).verifyComplete();
        verify(persistence).save(1L, 2L);
    }

    @Test
    void rejectsDuplicateAndOverlappingEnrollment() {
        Bootcamp existing = bootcamp(1L, "2026-11-01", 5);
        when(persistence.findBootcamp(1L)).thenReturn(Mono.just(existing));
        when(persistence.findEnrolledBootcamps(1L)).thenReturn(Flux.just(existing));
        StepVerifier.create(useCase.enroll(1L, 1L)).expectError(ConflictException.class).verify();

        when(persistence.findBootcamp(2L)).thenReturn(Mono.just(bootcamp(2L, "2026-11-05", 5)));
        StepVerifier.create(useCase.enroll(1L, 2L)).expectError(ConflictException.class).verify();
        verify(persistence, never()).save(1L, 1L);
        verify(persistence, never()).save(1L, 2L);
    }

    @Test
    void limitsFiveCurrentEnrollmentsButIgnoresFinishedOnes() {
        Bootcamp requested = bootcamp(7L, "2027-01-01", 5);
        when(persistence.findBootcamp(7L)).thenReturn(Mono.just(requested));
        when(persistence.findEnrolledBootcamps(1L)).thenReturn(Flux.fromIterable(List.of(
                bootcamp(1L, "2026-01-01", 3),
                bootcamp(2L, "2026-11-01", 3), bootcamp(3L, "2026-11-11", 3),
                bootcamp(4L, "2026-11-21", 3), bootcamp(5L, "2026-12-01", 3),
                bootcamp(6L, "2026-12-11", 3))));

        StepVerifier.create(useCase.enroll(1L, 7L)).expectError(ConflictException.class).verify();
        verify(persistence, never()).save(1L, 7L);
    }

    @Test
    void rejectsMissingBootcamp() {
        when(persistence.findBootcamp(9L)).thenReturn(Mono.empty());
        StepVerifier.create(useCase.enroll(1L, 9L)).expectError(NotFoundException.class).verify();
    }

    private Bootcamp bootcamp(Long id, String start, int days) {
        return new Bootcamp(id, "Bootcamp " + id, "Description", LocalDate.parse(start), days, List.of());
    }
}
