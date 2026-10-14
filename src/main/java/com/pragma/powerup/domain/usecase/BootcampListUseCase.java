package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IBootcampListServicePort;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.BootcampListItem;
import com.pragma.powerup.domain.model.BootcampPage;
import com.pragma.powerup.domain.model.BootcampPageQuery;
import com.pragma.powerup.domain.model.CapacityDetail;
import com.pragma.powerup.domain.spi.IBootcampQueryPort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BootcampListUseCase implements IBootcampListServicePort {
    private final IBootcampQueryPort bootcampPort;
    private final ICapacityQueryPort capacityPort;

    @Override
    public Mono<BootcampPage> list(BootcampPageQuery query) {
        return Mono.zip(bootcampPort.count(), bootcampPort.findPage(query).collectList())
                .flatMap(result -> {
                    List<Bootcamp> bootcamps = result.getT2();
                    List<Long> capacityIds = bootcamps.stream().flatMap(bootcamp -> bootcamp.getCapacityIds().stream())
                            .distinct().toList();
                    Mono<Map<Long, CapacityDetail>> details = capacityIds.isEmpty()
                            ? Mono.just(Map.of()) : capacityPort.findDetailsByIds(capacityIds);
                    return details.map(capacities -> toPage(bootcamps, capacities, result.getT1(), query));
                });
    }

    private BootcampPage toPage(List<Bootcamp> bootcamps, Map<Long, CapacityDetail> capacities,
            long total, BootcampPageQuery query) {
        List<BootcampListItem> items = bootcamps.stream()
                .map(bootcamp -> new BootcampListItem(bootcamp.getId(), bootcamp.getName(),
                        bootcamp.getDescription(), bootcamp.getLaunchDate(), bootcamp.getDurationDays(),
                        bootcamp.getCapacityIds().stream().map(id -> detail(capacities, id)).toList()))
                .toList();
        long pages = total / query.getSize() + (total % query.getSize() == 0 ? 0 : 1);
        return new BootcampPage(items, query.getPage(), query.getSize(), total, pages);
    }

    private CapacityDetail detail(Map<Long, CapacityDetail> capacities, Long id) {
        CapacityDetail capacity = capacities.get(id);
        if (capacity == null) {
            throw new ExternalServiceException(ExceptionMessages.CAPACITY_DETAILS_UNAVAILABLE.getMessage());
        }
        return capacity;
    }
}
