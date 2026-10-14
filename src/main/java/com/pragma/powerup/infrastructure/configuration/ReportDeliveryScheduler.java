package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IReportDeliveryServicePort;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "report.delivery.enabled", havingValue = "true", matchIfMissing = true)
public class ReportDeliveryScheduler {
    private final IReportDeliveryServicePort delivery;
    private final AtomicBoolean running = new AtomicBoolean();

    @Value("${report.delivery.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${report.delivery.delay-ms:5000}")
    public void deliver() {
        if (running.compareAndSet(false, true)) {
            delivery.deliverPending(batchSize)
                    .doOnError(error -> log.warn("Report delivery will be retried", error))
                    .onErrorComplete()
                    .doFinally(signal -> running.set(false))
                    .subscribe();
        }
    }
}
