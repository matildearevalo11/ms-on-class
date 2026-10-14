package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IBootcampPopularityServicePort;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.model.CapacityDetail;
import com.pragma.powerup.domain.model.MostEnrolledBootcamp;
import com.pragma.powerup.domain.model.MostEnrolledBootcampDetails;
import com.pragma.powerup.domain.model.PersonSummary;
import com.pragma.powerup.domain.spi.IBootcampPopularityQueryPort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import com.pragma.powerup.domain.spi.IPersonQueryPort;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BootcampPopularityUseCase implements IBootcampPopularityServicePort {
    private final IBootcampPopularityQueryPort bootcamps;
    private final ICapacityQueryPort capacities;
    private final IPersonQueryPort persons;

    @Override
    public Mono<MostEnrolledBootcampDetails> findMostEnrolled() {
        return bootcamps.findMostEnrolled()
                .switchIfEmpty(Mono.error(new NotFoundException(ExceptionMessages.BOOTCAMP_NOT_FOUND.getMessage())))
                .flatMap(winner -> Mono.zip(personDetails(winner.getPersonIds()),
                                capacityDetails(winner.getBootcamp().getCapacityIds()))
                        .map(details -> new MostEnrolledBootcampDetails(winner.getBootcamp(),
                                winner.getPersonIds().stream().map(id -> person(details.getT1(), id)).toList(),
                                winner.getBootcamp().getCapacityIds().stream()
                                        .map(id -> capacity(details.getT2(), id)).toList())));
    }

    private Mono<Map<Long, PersonSummary>> personDetails(List<Long> ids) {
        return ids.isEmpty() ? Mono.just(Map.of()) : persons.findByIds(ids);
    }

    private Mono<Map<Long, CapacityDetail>> capacityDetails(List<Long> ids) {
        return ids.isEmpty() ? Mono.just(Map.of()) : capacities.findDetailsByIds(ids);
    }

    private PersonSummary person(Map<Long, PersonSummary> details, Long id) {
        PersonSummary person = details.get(id);
        if (person == null || person.getName() == null || person.getName().isBlank()) {
            throw new ExternalServiceException(ExceptionMessages.PERSON_DETAILS_UNAVAILABLE.getMessage());
        }
        return person;
    }

    private CapacityDetail capacity(Map<Long, CapacityDetail> details, Long id) {
        CapacityDetail capacity = details.get(id);
        if (capacity == null) {
            throw new ExternalServiceException(ExceptionMessages.CAPACITY_DETAILS_UNAVAILABLE.getMessage());
        }
        return capacity;
    }
}
