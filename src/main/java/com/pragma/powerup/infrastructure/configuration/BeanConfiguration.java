package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IBootcampServicePort;
import com.pragma.powerup.domain.api.IBootcampListServicePort;
import com.pragma.powerup.domain.spi.IBootcampQueryPort;
import com.pragma.powerup.domain.usecase.BootcampListUseCase;
import com.pragma.powerup.domain.spi.IBootcampPersistencePort;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import com.pragma.powerup.domain.usecase.BootcampUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Bean
    IBootcampListServicePort bootcampListServicePort(IBootcampQueryPort bootcampPort,
            ICapacityQueryPort capacityPort) {
        return new BootcampListUseCase(bootcampPort, capacityPort);
    }

    @Bean
    IBootcampServicePort bootcampServicePort(IBootcampPersistencePort persistencePort,
            ICapacityQueryPort capacityPort) {
        return new BootcampUseCase(persistencePort, capacityPort);
    }
}
