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
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BootcampUseCase implements IBootcampServicePort {
    private final IBootcampPersistencePort persistencePort;
    private final ICapacityQueryPort capacityPort;
    private final IReportOutboxPort reportOutbox;

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
        return capacityPort.findDetailsByIds(bootcamp.getCapacityIds())
                .flatMap(details -> details.keySet().containsAll(bootcamp.getCapacityIds())
                        ? persistencePort.save(bootcamp)
                                .flatMap(saved -> reportOutbox.enqueue(new BootcampReportSnapshot(saved,
                                        orderedCapacities(saved.getCapacityIds(), details))).thenReturn(saved))
                        : Mono.error(new ValidationException(ExceptionMessages.CAPACITIES_NOT_FOUND.getMessage())));
    }

    private List<CapacityDetail> orderedCapacities(List<Long> ids, Map<Long, CapacityDetail> details) {
        return ids.stream().map(details::get).toList();
    }
}
