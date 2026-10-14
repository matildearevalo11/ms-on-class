package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.CapacityDetail;
import com.pragma.powerup.domain.model.MostEnrolledBootcamp;
import com.pragma.powerup.domain.model.PersonSummary;
import com.pragma.powerup.domain.model.TechnologySummary;
import com.pragma.powerup.domain.spi.IBootcampPopularityQueryPort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import com.pragma.powerup.domain.spi.IPersonQueryPort;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BootcampPopularityUseCaseTest {
    private final IBootcampPopularityQueryPort bootcamps = mock(IBootcampPopularityQueryPort.class);
    private final ICapacityQueryPort capacities = mock(ICapacityQueryPort.class);
    private final IPersonQueryPort persons = mock(IPersonQueryPort.class);
    private final BootcampPopularityUseCase useCase = new BootcampPopularityUseCase(bootcamps, capacities, persons);

    @Test
    void enrichesWinnerInEnrollmentAndCapacityOrder() {
        var bootcamp = new Bootcamp(9L, "Backend", "Description", LocalDate.of(2026, 11, 1), 30,
                List.of(5L));
        when(bootcamps.findMostEnrolled()).thenReturn(Mono.just(new MostEnrolledBootcamp(bootcamp, List.of(2L, 1L))));
        when(persons.findByIds(List.of(2L, 1L))).thenReturn(Mono.just(Map.of(
                1L, new PersonSummary(1L, "First", "first@example.com"),
                2L, new PersonSummary(2L, "Second", "second@example.com"))));
        when(capacities.findDetailsByIds(List.of(5L))).thenReturn(Mono.just(Map.of(5L,
                new CapacityDetail(5L, "Backend", List.of(new TechnologySummary(7L, "Java"))))));

        StepVerifier.create(useCase.findMostEnrolled()).assertNext(result -> {
            assertThat(result.getPersonCount()).isEqualTo(2);
            assertThat(result.getPersons()).extracting(PersonSummary::getId).containsExactly(2L, 1L);
            assertThat(result.getCapacities().getFirst().getTechnologies().getFirst().getName()).isEqualTo("Java");
        }).verifyComplete();
    }

    @Test
    void rejectsIncompletePersonDetails() {
        var bootcamp = new Bootcamp(9L, "Backend", "Description", LocalDate.of(2026, 11, 1), 30,
                List.of());
        when(bootcamps.findMostEnrolled()).thenReturn(Mono.just(new MostEnrolledBootcamp(bootcamp, List.of(1L))));
        when(persons.findByIds(List.of(1L))).thenReturn(Mono.just(Map.of()));

        StepVerifier.create(useCase.findMostEnrolled()).expectError(ExternalServiceException.class).verify();
    }
}
