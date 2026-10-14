package com.pragma.powerup.infrastructure.out.r2dbc.adapter;

import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.model.MostEnrolledBootcamp;
import com.pragma.powerup.domain.spi.IBootcampPopularityQueryPort;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class BootcampPopularityR2dbcAdapter implements IBootcampPopularityQueryPort {
    private final DatabaseClient database;

    @Override
    public Mono<MostEnrolledBootcamp> findMostEnrolled() {
        return database.sql("SELECT b.id, b.name, b.description, b.launch_date, b.duration_days "
                        + "FROM bootcamps b LEFT JOIN bootcamp_enrollments e ON e.bootcamp_id = b.id "
                        + "GROUP BY b.id, b.name, b.description, b.launch_date, b.duration_days "
                        + "ORDER BY COUNT(e.person_id) DESC, b.id ASC LIMIT 1")
                .map((row, metadata) -> new Bootcamp(row.get("id", Long.class), row.get("name", String.class),
                        row.get("description", String.class), row.get("launch_date", LocalDate.class),
                        row.get("duration_days", Integer.class), List.of())).one()
                .flatMap(bootcamp -> Mono.zip(capacityIds(bootcamp.getId()), personIds(bootcamp.getId()))
                        .map(ids -> new MostEnrolledBootcamp(new Bootcamp(bootcamp.getId(), bootcamp.getName(),
                                bootcamp.getDescription(), bootcamp.getLaunchDate(), bootcamp.getDurationDays(),
                                ids.getT1()), ids.getT2())));
    }

    private Mono<List<Long>> capacityIds(Long bootcampId) {
        return database.sql("SELECT capacity_id FROM bootcamp_capacities WHERE bootcamp_id = :id ORDER BY capacity_id")
                .bind("id", bootcampId).map((row, metadata) -> row.get("capacity_id", Long.class))
                .all().collectList();
    }

    private Mono<List<Long>> personIds(Long bootcampId) {
        return database.sql("SELECT person_id FROM bootcamp_enrollments WHERE bootcamp_id = :id ORDER BY person_id")
                .bind("id", bootcampId).map((row, metadata) -> row.get("person_id", Long.class))
                .all().collectList();
    }
}
