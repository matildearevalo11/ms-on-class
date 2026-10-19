package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IBootcampServicePort;
import com.pragma.powerup.domain.exception.ConflictException;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.BootcampCapacityUsage;
import com.pragma.powerup.domain.model.BootcampReportSnapshot;
import com.pragma.powerup.domain.model.CapacityDetail;
import com.pragma.powerup.domain.model.TechnologySummary;
import com.pragma.powerup.domain.spi.IBootcampPersistencePort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import com.pragma.powerup.domain.spi.IReportOutboxPort;
import com.pragma.powerup.domain.util.ApplicationConstants;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        return persistencePort.findCapacityUsage(id)
                .switchIfEmpty(Mono.error(new NotFoundException(ExceptionMessages.BOOTCAMP_NOT_FOUND.getMessage())))
                .flatMap(usage -> rejectSharedResources(id, usage).thenReturn(usage.getCapacityIds()))
                .flatMap(capacityIds -> persistencePort.deleteById(id)
                        .then(Mono.defer(() -> reportOutbox.discardPending(id)))
                        .then(Mono.defer(() -> capacityIds.isEmpty()
                                ? Mono.<Void>empty() : capacityPort.deleteCapacities(capacityIds))));
    }

    private Mono<Void> rejectSharedResources(Long bootcampId, BootcampCapacityUsage usage) {
        if (usage.isSharesCapacities()) {
            return Mono.error(new ConflictException(ExceptionMessages.BOOTCAMP_SHARES_CAPACITIES.getMessage()));
        }
        if (usage.getCapacityIds().isEmpty()) {
            return Mono.empty();
        }
        return persistencePort.findCapacityIdsOfOtherBootcamps(bootcampId).collectList()
                .flatMap(otherCapacityIds -> otherCapacityIds.isEmpty() ? Mono.<Void>empty()
                        : rejectSharedTechnologies(usage.getCapacityIds(), otherCapacityIds));
    }

    private Mono<Void> rejectSharedTechnologies(List<Long> capacityIds, List<Long> otherCapacityIds) {
        List<Long> allCapacityIds = Stream.concat(capacityIds.stream(), otherCapacityIds.stream()).toList();
        return capacityPort.findDetailsByIds(allCapacityIds)
                .flatMap(details -> {
                    Set<Long> otherTechnologies = technologyIds(details, otherCapacityIds);
                    return technologyIds(details, capacityIds).stream().anyMatch(otherTechnologies::contains)
                            ? Mono.error(new ConflictException(ExceptionMessages.BOOTCAMP_SHARES_TECHNOLOGIES.getMessage()))
                            : Mono.<Void>empty();
                });
    }

    private Set<Long> technologyIds(Map<Long, CapacityDetail> details, List<Long> capacityIds) {
        return capacityIds.stream()
                .map(details::get)
                .filter(Objects::nonNull)
                .flatMap(capacity -> capacity.getTechnologies().stream())
                .map(TechnologySummary::getId)
                .collect(Collectors.toSet());
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
