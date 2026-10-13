package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IBootcampServicePort;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.spi.IBootcampPersistencePort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BootcampUseCase implements IBootcampServicePort {
    private final IBootcampPersistencePort persistencePort;
    private final ICapacityQueryPort capacityPort;

    @Override
    public Mono<Void> deleteBootcamp(Long id) {
        return persistencePort.existsById(id)
                .flatMap(exists -> exists
                        ? persistencePort.findExclusiveCapacityIds(id).collectList()
                                .flatMap(capacityIds -> (capacityIds.isEmpty()
                                        ? Mono.<Void>empty() : capacityPort.deleteCapacities(capacityIds))
                                        .then(Mono.defer(() -> persistencePort.deleteById(id))))
                        : Mono.error(new NotFoundException(ExceptionMessages.BOOTCAMP_NOT_FOUND.getMessage())));
    }

    @Override
    public Mono<Bootcamp> createBootcamp(Bootcamp bootcamp) {
        return capacityPort.findExistingIds(bootcamp.getCapacityIds())
                .flatMap(existing -> existing.containsAll(bootcamp.getCapacityIds())
                        ? persistencePort.save(bootcamp)
                        : Mono.error(new ValidationException(ExceptionMessages.CAPACITIES_NOT_FOUND.getMessage())));
    }
}
