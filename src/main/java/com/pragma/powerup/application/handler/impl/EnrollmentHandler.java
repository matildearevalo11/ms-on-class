package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.handler.IEnrollmentHandler;
import com.pragma.powerup.domain.api.IEnrollmentServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EnrollmentHandler implements IEnrollmentHandler {
    private final IEnrollmentServicePort servicePort;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Mono<Void> enroll(Long personId, Long bootcampId) {
        return servicePort.enroll(personId, bootcampId);
    }
}
