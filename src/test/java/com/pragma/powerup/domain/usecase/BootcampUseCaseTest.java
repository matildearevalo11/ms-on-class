package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.spi.IBootcampPersistencePort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;

class BootcampUseCaseTest {
    @Test
    void savesOnlyWhenEveryCapacityExists() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        var input = new Bootcamp(null, "Backend", "Services", LocalDate.of(2026, 11, 1), 90, List.of(1L, 2L));
        var saved = new Bootcamp(7L, input.getName(), input.getDescription(), input.getLaunchDate(),
                input.getDurationDays(), input.getCapacityIds());
        when(capacities.findExistingIds(input.getCapacityIds())).thenReturn(Mono.just(List.of(2L, 1L)));
        when(persistence.save(input)).thenReturn(Mono.just(saved));

        StepVerifier.create(new BootcampUseCase(persistence, capacities).createBootcamp(input))
                .expectNext(saved).verifyComplete();
        verify(persistence).save(input);
    }

    @Test
    void refusesToSaveWhenACapacityIsMissing() {
        var persistence = mock(IBootcampPersistencePort.class);
        var capacities = mock(ICapacityQueryPort.class);
        var input = new Bootcamp(null, "Backend", "Services", LocalDate.of(2026, 11, 1), 90, List.of(1L, 2L));
        when(capacities.findExistingIds(input.getCapacityIds())).thenReturn(Mono.just(List.of(1L)));

        StepVerifier.create(new BootcampUseCase(persistence, capacities).createBootcamp(input))
                .expectError(ValidationException.class).verify();
        verifyNoInteractions(persistence);
    }
}
