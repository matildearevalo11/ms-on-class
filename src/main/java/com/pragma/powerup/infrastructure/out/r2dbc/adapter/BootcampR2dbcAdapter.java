package com.pragma.powerup.infrastructure.out.r2dbc.adapter;

import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.spi.IBootcampPersistencePort;
import com.pragma.powerup.infrastructure.out.r2dbc.entity.BootcampEntity;
import com.pragma.powerup.infrastructure.out.r2dbc.repository.IBootcampRepository;
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
}
