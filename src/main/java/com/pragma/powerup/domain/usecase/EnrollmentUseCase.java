package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IEnrollmentServicePort;
import com.pragma.powerup.domain.exception.ConflictException;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.util.ApplicationConstants;
import com.pragma.powerup.domain.spi.IEnrollmentPersistencePort;
import com.pragma.powerup.domain.spi.IReportOutboxPort;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class EnrollmentUseCase implements IEnrollmentServicePort {
    private final IEnrollmentPersistencePort persistencePort;
    private final IReportOutboxPort reportOutbox;
    private final Clock clock;

    @Override
    public Mono<Void> enroll(Long personId, Long bootcampId) {
        return persistencePort.findBootcamp(bootcampId)
                .switchIfEmpty(Mono.error(new NotFoundException(ExceptionMessages.BOOTCAMP_NOT_FOUND.getMessage())))
                .flatMap(bootcamp -> persistencePort.findEnrolledBootcamps(personId).collectList()
                        .flatMap(enrolled -> validateAndSave(personId, bootcamp, enrolled)));
    }

    private Mono<Void> validateAndSave(Long personId, Bootcamp bootcamp, List<Bootcamp> enrolled) {
        if (enrolled.stream().anyMatch(existing -> existing.getId().equals(bootcamp.getId()))) {
            return Mono.error(new ConflictException(ExceptionMessages.ALREADY_ENROLLED.getMessage()));
        }
        LocalDate today = LocalDate.now(clock);
        long current = enrolled.stream().filter(existing -> endDate(existing).isAfter(today)).count();
        if (current >= ApplicationConstants.MAX_ACTIVE_BOOTCAMPS.getValue() && endDate(bootcamp).isAfter(today)) {
            return Mono.error(new ConflictException(ExceptionMessages.ENROLLMENT_LIMIT.getMessage()));
        }
        if (enrolled.stream().anyMatch(existing -> overlaps(existing, bootcamp))) {
            return Mono.error(new ConflictException(ExceptionMessages.ENROLLMENT_OVERLAP.getMessage()));
        }
        return persistencePort.save(personId, bootcamp.getId())
                .then(Mono.defer(() -> reportOutbox.enqueuePersonCountChange(bootcamp.getId())));
    }

    private boolean overlaps(Bootcamp first, Bootcamp second) {
        return first.getLaunchDate().isBefore(endDate(second))
                && second.getLaunchDate().isBefore(endDate(first));
    }

    private LocalDate endDate(Bootcamp bootcamp) {
        return bootcamp.getLaunchDate().plusDays(bootcamp.getDurationDays());
    }
}
