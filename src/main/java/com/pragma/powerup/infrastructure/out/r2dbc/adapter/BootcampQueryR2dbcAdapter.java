package com.pragma.powerup.infrastructure.out.r2dbc.adapter;

import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.BootcampPageQuery;
import com.pragma.powerup.domain.spi.IBootcampQueryPort;
import com.pragma.powerup.infrastructure.out.r2dbc.entity.BootcampEntity;
import com.pragma.powerup.infrastructure.out.r2dbc.repository.IBootcampRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class BootcampQueryR2dbcAdapter implements IBootcampQueryPort {
    private final IBootcampRepository repository;
    private final DatabaseClient database;

    @Override
    public Mono<Long> count() {
        return repository.count();
    }

    @Override
    public Flux<Bootcamp> findPage(BootcampPageQuery query) {
        long offset = (long) query.getPage() * query.getSize();
        Flux<BootcampEntity> rows = pageRows(query, offset);
        return rows.collectList().flatMapMany(entities -> {
            if (entities.isEmpty()) {
                return Flux.empty();
            }
            List<Long> ids = entities.stream().map(BootcampEntity::getId).toList();
            return database.sql("SELECT bootcamp_id, capacity_id FROM bootcamp_capacities "
                            + "WHERE bootcamp_id IN (:ids) ORDER BY bootcamp_id, capacity_id")
                    .bind("ids", ids)
                    .map((row, metadata) -> Map.entry(row.get("bootcamp_id", Long.class),
                            row.get("capacity_id", Long.class)))
                    .all().collectMultimap(Map.Entry::getKey, Map.Entry::getValue)
                    .flatMapMany(capacities -> Flux.fromIterable(entities)
                            .map(entity -> new Bootcamp(entity.getId(), entity.getName(), entity.getDescription(),
                                    entity.getLaunchDate(), entity.getDurationDays(),
                                    List.copyOf(capacities.getOrDefault(entity.getId(), List.of())))));
        });
    }

    private Flux<BootcampEntity> pageRows(BootcampPageQuery query, long offset) {
        if ("name".equals(query.getSortBy())) {
            return query.isDescending() ? repository.findPageByNameDesc(query.getSize(), offset)
                    : repository.findPageByNameAsc(query.getSize(), offset);
        }
        return query.isDescending() ? repository.findPageByCapacityCountDesc(query.getSize(), offset)
                : repository.findPageByCapacityCountAsc(query.getSize(), offset);
    }
}
