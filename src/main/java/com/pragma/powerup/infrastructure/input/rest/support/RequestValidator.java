package com.pragma.powerup.infrastructure.input.rest.support;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {
    private final Validator validator;

    public <T> Mono<T> body(ServerRequest request, Class<T> type) {
        return request.bodyToMono(type)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Request body is required.")))
                .flatMap(this::validate);
    }

    public <T> Mono<T> validate(T target) {
        return Mono.defer(() -> validator.validate(target).stream()
                .min(Comparator.comparing((ConstraintViolation<T> violation) -> violation.getPropertyPath().toString()))
                .<Mono<T>>map(violation -> Mono.error(new RequestValidationException(violation.getMessage())))
                .orElseGet(() -> Mono.just(target)));
    }
}
