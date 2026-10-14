package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.spi.ICapacityQueryPort;
import com.pragma.powerup.domain.model.CapacityDetail;
import com.pragma.powerup.domain.model.TechnologySummary;
import com.pragma.powerup.infrastructure.out.rest.dto.CapacityDetailResponseDto;
import java.time.Duration;
import java.util.List;
import java.util.Map;
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
import java.util.function.Function;

@Component
public class CapacityQueryRestAdapter implements ICapacityQueryPort {
    private final WebClient client;
    private final Duration timeout;

    @Override
    public Mono<Void> deleteCapacities(List<Long> ids) {
        return currentJwt()
                .flatMap(jwt -> client.delete().uri(builder -> builder.path("/capacities")
                                .queryParam("ids", ids.stream().map(String::valueOf).collect(Collectors.joining(",")))
                                .build())
                        .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                        .retrieve().toBodilessEntity())
                .timeout(timeout)
                .onErrorMap(error -> error instanceof WebClientException || error instanceof TimeoutException,
                        error -> new ExternalServiceException(ExceptionMessages.CAPACITY_SERVICE_UNAVAILABLE.getMessage()))
                .then();
    }

    public CapacityQueryRestAdapter(WebClient.Builder builder,
            @Value("${services.capacity.url}") String url,
            @Value("${services.capacity.timeout}") Duration timeout) {
        this.client = builder.baseUrl(url).build();
        this.timeout = timeout;
    }

    @Override
    public Mono<List<Long>> findExistingIds(List<Long> ids) {
        return currentJwt()
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

    @Override
    public Mono<Map<Long, CapacityDetail>> findDetailsByIds(List<Long> ids) {
        return currentJwt()
                .flatMap(jwt -> client.get().uri(builder -> builder.path("/capacities/details")
                                .queryParam("ids", ids.stream().map(String::valueOf).collect(Collectors.joining(",")))
                                .build())
                        .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                        .retrieve()
                        .bodyToFlux(CapacityDetailResponseDto.class).collectList())
                .timeout(timeout)
                .onErrorMap(error -> error instanceof WebClientException || error instanceof TimeoutException,
                        error -> new ExternalServiceException(ExceptionMessages.CAPACITY_SERVICE_UNAVAILABLE.getMessage()))
                .map(details -> details.stream().map(detail -> new CapacityDetail(detail.getId(), detail.getName(),
                                detail.getTechnologies().stream()
                                        .map(technology -> new TechnologySummary(technology.getId(), technology.getName()))
                                        .toList()))
                        .collect(Collectors.toMap(CapacityDetail::getId, Function.identity())));
    }

    private Mono<Jwt> currentJwt() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> (Jwt) context.getAuthentication().getPrincipal())
                .switchIfEmpty(Mono.error(new ExternalServiceException(ExceptionMessages.AUTHENTICATION_REQUIRED.getMessage())));
    }
}
