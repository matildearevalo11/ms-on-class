package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.enums.BootcampSortField;
import com.pragma.powerup.domain.enums.SortDirection;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.BootcampPageQuery;
import com.pragma.powerup.domain.model.CapacityDetail;
import com.pragma.powerup.domain.model.TechnologySummary;
import com.pragma.powerup.domain.spi.IBootcampQueryPort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;

class BootcampListUseCaseTest {
    @Test
    void enrichesOnlyTheRequestedPageAndCalculatesTotals() {
        var bootcamps = mock(IBootcampQueryPort.class);
        var capacities = mock(ICapacityQueryPort.class);
        var query = new BootcampPageQuery(1, 2, BootcampSortField.CAPACITY_COUNT, SortDirection.DESC);
        var bootcamp = new Bootcamp(5L, "Backend", "Services", LocalDate.of(2026, 11, 1), 90,
                List.of(1L, 2L));
        when(bootcamps.count()).thenReturn(Mono.just(3L));
        when(bootcamps.findPage(query)).thenReturn(Flux.just(bootcamp));
        when(capacities.findDetailsByIds(List.of(1L, 2L))).thenReturn(Mono.just(Map.of(
                1L, new CapacityDetail(1L, "Java", List.of(new TechnologySummary(7L, "JDK"))),
                2L, new CapacityDetail(2L, "SQL", List.of()))));

        StepVerifier.create(new BootcampListUseCase(bootcamps, capacities).list(query))
                .assertNext(page -> {
                    org.assertj.core.api.Assertions.assertThat(page.getTotalPages()).isEqualTo(2);
                    org.assertj.core.api.Assertions.assertThat(page.getContent()).hasSize(1);
                    org.assertj.core.api.Assertions.assertThat(page.getContent().getFirst().getCapacities())
                            .extracting(CapacityDetail::getName).containsExactly("Java", "SQL");
                }).verifyComplete();
        verify(capacities).findDetailsByIds(List.of(1L, 2L));
    }

    @Test
    void reportsMissingCapacityDetailsInsteadOfReturningAnIncompleteBootcamp() {
        var bootcamps = mock(IBootcampQueryPort.class);
        var capacities = mock(ICapacityQueryPort.class);
        var query = new BootcampPageQuery(0, 10, BootcampSortField.NAME, SortDirection.ASC);
        when(bootcamps.count()).thenReturn(Mono.just(1L));
        when(bootcamps.findPage(query)).thenReturn(Flux.just(new Bootcamp(5L, "Backend", "Services",
                LocalDate.of(2026, 11, 1), 90, List.of(1L))));
        when(capacities.findDetailsByIds(List.of(1L))).thenReturn(Mono.just(Map.of()));

        StepVerifier.create(new BootcampListUseCase(bootcamps, capacities).list(query))
                .expectError(ExternalServiceException.class).verify();
    }
}
