package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

@Component
public class CapacityQueryRestAdapter implements ICapacityQueryPort {
    private final WebClient client;
    private final Duration timeout;

    public CapacityQueryRestAdapter(WebClient.Builder builder,
            @Value("${services.capacity.url}") String url,
            @Value("${services.capacity.timeout}") Duration timeout) {
        this.client = builder.baseUrl(url).build();
        this.timeout = timeout;
    }

    @Override
    public Mono<List<Long>> findExistingIds(List<Long> ids) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> (Jwt) context.getAuthentication().getPrincipal())
                .switchIfEmpty(Mono.error(new ExternalServiceException(ExceptionMessages.AUTHENTICATION_REQUIRED.getMessage())))
                .flatMap(jwt -> client.get().uri(builder -> builder.path("/capacities/ids")
                                .queryParam("ids", ids.stream().map(String::valueOf).collect(Collectors.joining(",")))
                                .build())
                        .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError,
                                response -> Mono.just(new ValidationException(ExceptionMessages.CAPACITIES_NOT_FOUND.getMessage())))
                        .bodyToFlux(Long.class).collectList())
                .timeout(timeout)
                .onErrorMap(error -> error instanceof WebClientException || error instanceof TimeoutException,
                        error -> new ExternalServiceException(ExceptionMessages.CAPACITY_SERVICE_UNAVAILABLE.getMessage()));
    }
}
