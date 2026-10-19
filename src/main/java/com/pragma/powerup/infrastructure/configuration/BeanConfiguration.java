package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IBootcampServicePort;
import com.pragma.powerup.domain.api.IBootcampListServicePort;
import com.pragma.powerup.domain.spi.IBootcampQueryPort;
import com.pragma.powerup.domain.usecase.BootcampListUseCase;
import com.pragma.powerup.domain.spi.IBootcampPersistencePort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import com.pragma.powerup.domain.usecase.BootcampUseCase;
import com.pragma.powerup.domain.api.IEnrollmentServicePort;
import com.pragma.powerup.domain.spi.IEnrollmentPersistencePort;
import com.pragma.powerup.domain.usecase.EnrollmentUseCase;
import com.pragma.powerup.domain.api.IReportDeliveryServicePort;
import com.pragma.powerup.domain.spi.IReportDeliveryPort;
import com.pragma.powerup.domain.spi.IReportOutboxPort;
import com.pragma.powerup.domain.usecase.ReportDeliveryUseCase;
import com.pragma.powerup.domain.api.IBootcampPopularityServicePort;
import com.pragma.powerup.domain.spi.IBootcampPopularityQueryPort;
import com.pragma.powerup.domain.spi.IPersonQueryPort;
import com.pragma.powerup.domain.usecase.BootcampPopularityUseCase;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Bean
    IBootcampPopularityServicePort bootcampPopularityServicePort(IBootcampPopularityQueryPort bootcamps,
            ICapacityQueryPort capacities, IPersonQueryPort persons) {
        return new BootcampPopularityUseCase(bootcamps, capacities, persons);
    }
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    IEnrollmentServicePort enrollmentServicePort(IEnrollmentPersistencePort persistencePort,
            IReportOutboxPort outbox, Clock clock) {
        return new EnrollmentUseCase(persistencePort, outbox, clock);
    }
    @Bean
    IBootcampListServicePort bootcampListServicePort(IBootcampQueryPort bootcampPort,
            ICapacityQueryPort capacityPort) {
        return new BootcampListUseCase(bootcampPort, capacityPort);
    }

    @Bean
    IBootcampServicePort bootcampServicePort(IBootcampPersistencePort persistencePort,
            ICapacityQueryPort capacityPort, IReportOutboxPort outbox) {
        return new BootcampUseCase(persistencePort, capacityPort, outbox);
    }

    @Bean
    IReportDeliveryServicePort reportDeliveryServicePort(IReportOutboxPort outbox,
            IReportDeliveryPort delivery) {
        return new ReportDeliveryUseCase(outbox, delivery);
    }
}
