package com.pragma.powerup.infrastructure.out.r2dbc.adapter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.pragma.powerup.domain.model.BootcampReportSnapshot;
import com.pragma.powerup.domain.model.PendingPersonCount;
import com.pragma.powerup.domain.model.PendingReport;
import com.pragma.powerup.domain.spi.IReportOutboxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ReportOutboxR2dbcAdapter implements IReportOutboxPort {
    private final DatabaseClient database;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> enqueue(BootcampReportSnapshot snapshot) {
        return Mono.fromCallable(() -> serialize(snapshot))
                .flatMap(payload -> database.sql("INSERT INTO report_outbox (bootcamp_id, payload) "
                                + "VALUES (:bootcampId, :payload)")
                        .bind("bootcampId", snapshot.getBootcampId())
                        .bind("payload", payload).fetch().rowsUpdated())
                .then();
    }

    @Override
    public Flux<PendingReport> findPending(int limit) {
        return database.sql("SELECT id, payload FROM report_outbox WHERE delivered_at IS NULL "
                        + "ORDER BY id LIMIT :limit")
                .bind("limit", limit)
                .map((row, metadata) -> new PendingReport(
                        row.get("id", Long.class), row.get("payload", String.class))).all();
    }

    @Override
    public Mono<Void> markDelivered(Long id) {
        return database.sql("UPDATE report_outbox SET delivered_at = CURRENT_TIMESTAMP "
                        + "WHERE id = :id AND delivered_at IS NULL")
                .bind("id", id).fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Void> discardPending(Long bootcampId) {
        return database.sql("DELETE FROM report_outbox WHERE bootcamp_id = :bootcampId AND delivered_at IS NULL")
                .bind("bootcampId", bootcampId).fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Void> enqueuePersonCountChange(Long bootcampId) {
        return database.sql("INSERT INTO report_person_count_outbox (bootcamp_id) VALUES (:bootcampId)")
                .bind("bootcampId", bootcampId).fetch().rowsUpdated().then();
    }

    @Override
    public Flux<PendingPersonCount> findPendingPersonCounts(int limit) {
        return database.sql("SELECT MAX(o.id) AS event_id, o.bootcamp_id, "
                        + "(SELECT COUNT(*) FROM bootcamp_enrollments e WHERE e.bootcamp_id = o.bootcamp_id) AS person_count "
                        + "FROM report_person_count_outbox o WHERE o.delivered_at IS NULL "
                        + "GROUP BY o.bootcamp_id ORDER BY MAX(o.id) LIMIT :limit")
                .bind("limit", limit)
                .map((row, metadata) -> new PendingPersonCount(row.get("event_id", Long.class),
                        row.get("bootcamp_id", Long.class), row.get("person_count", Long.class)))
                .all();
    }

    @Override
    public Mono<Void> markPersonCountDelivered(PendingPersonCount event) {
        return database.sql("UPDATE report_person_count_outbox SET delivered_at = CURRENT_TIMESTAMP "
                        + "WHERE bootcamp_id = :bootcampId AND id <= :eventId AND delivered_at IS NULL")
                .bind("bootcampId", event.getBootcampId()).bind("eventId", event.getEventId())
                .fetch().rowsUpdated().then();
    }

    private String serialize(BootcampReportSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Cannot serialize bootcamp report", exception);
        }
    }
}
