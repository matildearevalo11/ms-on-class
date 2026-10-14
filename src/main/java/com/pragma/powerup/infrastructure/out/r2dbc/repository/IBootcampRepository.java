package com.pragma.powerup.infrastructure.out.r2dbc.repository;

import com.pragma.powerup.infrastructure.out.r2dbc.entity.BootcampEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;

public interface IBootcampRepository extends ReactiveCrudRepository<BootcampEntity, Long> {
    @Query("SELECT id, name, description, launch_date, duration_days FROM bootcamps ORDER BY name ASC, id ASC LIMIT :size OFFSET :offset")
    Flux<BootcampEntity> findPageByNameAsc(int size, long offset);

    @Query("SELECT id, name, description, launch_date, duration_days FROM bootcamps ORDER BY name DESC, id DESC LIMIT :size OFFSET :offset")
    Flux<BootcampEntity> findPageByNameDesc(int size, long offset);

    @Query("SELECT b.id, b.name, b.description, b.launch_date, b.duration_days FROM bootcamps b "
            + "LEFT JOIN bootcamp_capacities bc ON bc.bootcamp_id = b.id "
            + "GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_days "
            + "ORDER BY COUNT(bc.capacity_id) ASC, b.name ASC, b.id ASC LIMIT :size OFFSET :offset")
    Flux<BootcampEntity> findPageByCapacityCountAsc(int size, long offset);

    @Query("SELECT b.id, b.name, b.description, b.launch_date, b.duration_days FROM bootcamps b "
            + "LEFT JOIN bootcamp_capacities bc ON bc.bootcamp_id = b.id "
            + "GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_days "
            + "ORDER BY COUNT(bc.capacity_id) DESC, b.name DESC, b.id DESC LIMIT :size OFFSET :offset")
    Flux<BootcampEntity> findPageByCapacityCountDesc(int size, long offset);
}
