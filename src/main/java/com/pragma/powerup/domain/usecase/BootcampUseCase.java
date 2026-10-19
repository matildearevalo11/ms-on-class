package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IBootcampServicePort;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.BootcampReportSnapshot;
import com.pragma.powerup.domain.model.CapacityDetail;
import com.pragma.powerup.domain.spi.IBootcampPersistencePort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import com.pragma.powerup.domain.spi.IReportOutboxPort;
import com.pragma.powerup.domain.util.ApplicationConstants;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import static com.pragma.powerup.domain.util.DomainValidations.requirePositive;
import static com.pragma.powerup.domain.util.DomainValidations.requirePositiveIds;
import static com.pragma.powerup.domain.util.DomainValidations.requirePresent;
import static com.pragma.powerup.domain.util.DomainValidations.requireSizeBetween;
import static com.pragma.powerup.domain.util.DomainValidations.requireText;
import static com.pragma.powerup.domain.util.DomainValidations.requireUnique;

@RequiredArgsConstructor
public class BootcampUseCase implements IBootcampServicePort {
    private final IBootcampPersistencePort persistencePort;
    private final ICapacityQueryPort capacityPort;
    private final IReportOutboxPort reportOutbox;

    @Override
    public Mono<Void> deleteBootcamp(Long id) {
        return persistencePort.findExclusiveCapacityIds(id)
                .switchIfEmpty(Mono.error(new NotFoundException(ExceptionMessages.BOOTCAMP_NOT_FOUND.getMessage())))
                .flatMap(capacityIds -> persistencePort.deleteById(id)
                        .then(Mono.defer(() -> reportOutbox.discardPending(id)))
                        .then(Mono.defer(() -> capacityIds.isEmpty()
                                ? Mono.<Void>empty() : capacityPort.deleteCapacities(capacityIds))));
    }

    @Override
    public Mono<Bootcamp> createBootcamp(Bootcamp bootcamp) {
        return Mono.fromCallable(() -> validate(bootcamp))
                .flatMap(this::saveWithExistingCapacities);
    }

    private Mono<Bootcamp> saveWithExistingCapacities(Bootcamp bootcamp) {
        return capacityPort.findDetailsByIds(bootcamp.getCapacityIds())
                .flatMap(details -> details.keySet().containsAll(bootcamp.getCapacityIds())
                        ? persistencePort.save(bootcamp)
                                .flatMap(saved -> reportOutbox.enqueue(new BootcampReportSnapshot(saved,
                                        orderedCapacities(saved.getCapacityIds(), details))).thenReturn(saved))
                        : Mono.error(new ValidationException(ExceptionMessages.CAPACITIES_NOT_FOUND.getMessage())));
    }

    private Bootcamp validate(Bootcamp bootcamp) {
        String name = requireText(bootcamp.getName(), ExceptionMessages.BOOTCAMP_NAME_REQUIRED);
        String description = requireText(bootcamp.getDescription(), ExceptionMessages.BOOTCAMP_DESCRIPTION_REQUIRED);
        LocalDate launchDate = requirePresent(bootcamp.getLaunchDate(), ExceptionMessages.LAUNCH_DATE_REQUIRED);
        Integer durationDays = requirePositive(bootcamp.getDurationDays(), ExceptionMessages.DURATION_INVALID);
        List<Long> capacityIds = requireSizeBetween(bootcamp.getCapacityIds(),
                ApplicationConstants.MIN_CAPACITIES_PER_BOOTCAMP.getValue(),
                ApplicationConstants.MAX_CAPACITIES_PER_BOOTCAMP.getValue(),
                ExceptionMessages.CAPACITY_COUNT_OUT_OF_RANGE);
        requirePositiveIds(capacityIds, ExceptionMessages.CAPACITY_ID_INVALID);
        requireUnique(capacityIds, ExceptionMessages.CAPACITIES_REPEATED);
        return new Bootcamp(bootcamp.getId(), name, description, launchDate, durationDays, List.copyOf(capacityIds));
    }

    private List<CapacityDetail> orderedCapacities(List<Long> ids, Map<Long, CapacityDetail> details) {
        return ids.stream().map(details::get).toList();
    }
}
