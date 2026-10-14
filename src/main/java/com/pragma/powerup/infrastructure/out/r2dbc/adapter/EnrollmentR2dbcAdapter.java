package com.pragma.powerup.infrastructure.out.r2dbc.adapter;

import com.pragma.powerup.domain.model.Bootcamp;
import com.pragma.powerup.domain.spi.IEnrollmentPersistencePort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class EnrollmentR2dbcAdapter implements IEnrollmentPersistencePort {
    private final DatabaseClient database;

    @Override
    public Mono<Bootcamp> findBootcamp(Long bootcampId) {
        return database.sql("SELECT id, name, description, launch_date, duration_days FROM bootcamps WHERE id = :id")
                .bind("id", bootcampId).map((row, metadata) -> toModel(row)).one();
    }

    @Override
    public Flux<Bootcamp> findEnrolledBootcamps(Long personId) {
        return database.sql("SELECT b.id, b.name, b.description, b.launch_date, b.duration_days "
                        + "FROM bootcamp_enrollments e JOIN bootcamps b ON b.id = e.bootcamp_id "
                        + "WHERE e.person_id = :personId")
                .bind("personId", personId).map((row, metadata) -> toModel(row)).all();
    }

    @Override
    public Mono<Void> save(Long personId, Long bootcampId) {
        return database.sql("INSERT INTO bootcamp_enrollments (person_id, bootcamp_id) VALUES (:personId, :bootcampId)")
                .bind("personId", personId).bind("bootcampId", bootcampId)
                .fetch().rowsUpdated().then();
    }

    private Bootcamp toModel(Row row) {
        return new Bootcamp(row.get("id", Long.class), row.get("name", String.class),
                row.get("description", String.class), row.get("launch_date", java.time.LocalDate.class),
                row.get("duration_days", Integer.class), List.of());
    }
}
