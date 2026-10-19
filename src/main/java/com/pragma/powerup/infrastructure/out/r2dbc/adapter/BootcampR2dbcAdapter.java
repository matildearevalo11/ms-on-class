package com.pragma.powerup.infrastructure.out.r2dbc.adapter;

import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.BootcampCapacityUsage;
import com.pragma.powerup.domain.spi.IBootcampPersistencePort;
import com.pragma.powerup.infrastructure.out.r2dbc.entity.BootcampEntity;
import com.pragma.powerup.infrastructure.out.r2dbc.repository.IBootcampRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class BootcampR2dbcAdapter implements IBootcampPersistencePort {
    private final IBootcampRepository repository;
    private final DatabaseClient database;

    @Override
    public Mono<BootcampCapacityUsage> findCapacityUsage(Long bootcampId) {
        return database.sql("SELECT b.id, bc.capacity_id, EXISTS ("
                        + "SELECT 1 FROM bootcamp_capacities other "
                        + "WHERE other.capacity_id = bc.capacity_id AND other.bootcamp_id <> b.id) AS shared "
                        + "FROM bootcamps b LEFT JOIN bootcamp_capacities bc ON bc.bootcamp_id = b.id "
                        + "WHERE b.id = :bootcampId")
                .bind("bootcampId", bootcampId)
                .map((row, metadata) -> new CapacityRow(row.get("capacity_id", Long.class),
                        Boolean.TRUE.equals(row.get("shared", Boolean.class))))
                .all().collectList()
                .filter(rows -> !rows.isEmpty())
                .map(rows -> new BootcampCapacityUsage(
                        rows.stream().map(CapacityRow::capacityId).filter(Objects::nonNull).toList(),
                        rows.stream().anyMatch(CapacityRow::shared)));
    }

    @Override
    public Flux<Long> findCapacityIdsOfOtherBootcamps(Long bootcampId) {
        return database.sql("SELECT DISTINCT capacity_id FROM bootcamp_capacities WHERE bootcamp_id <> :bootcampId")
                .bind("bootcampId", bootcampId)
                .map((row, metadata) -> row.get("capacity_id", Long.class)).all();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        var entity = new BootcampEntity(null, bootcamp.getName(), bootcamp.getDescription(),
                bootcamp.getLaunchDate(), bootcamp.getDurationDays());
        return repository.save(entity)
                .flatMap(saved -> Flux.fromIterable(bootcamp.getCapacityIds())
                        .concatMap(capacityId -> database.sql("INSERT INTO bootcamp_capacities (bootcamp_id, capacity_id) VALUES (:bootcampId, :capacityId)")
                                .bind("bootcampId", saved.getId())
                                .bind("capacityId", capacityId)
                                .fetch().rowsUpdated())
                        .then().thenReturn(new Bootcamp(saved.getId(), saved.getName(), saved.getDescription(),
                                saved.getLaunchDate(), saved.getDurationDays(), bootcamp.getCapacityIds())));
    }

    private record CapacityRow(Long capacityId, boolean shared) {
    }
}
