package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.infrastructure.constants.ApiParameters;
import com.pragma.powerup.infrastructure.constants.ApiPaths;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.model.PersonSummary;
import com.pragma.powerup.domain.spi.IPersonQueryPort;
import com.pragma.powerup.infrastructure.out.rest.dto.PersonSummaryResponseDto;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

@Component
public class PersonQueryRestAdapter implements IPersonQueryPort {
    private final WebClient client;
    private final Duration timeout;

    public PersonQueryRestAdapter(WebClient.Builder builder,
            @Value("${services.person.url}") String url,
            @Value("${services.person.timeout}") Duration timeout) {
        this.client = builder.baseUrl(url).build();
        this.timeout = timeout;
    }

    @Override
    public Mono<Map<Long, PersonSummary>> findByIds(List<Long> ids) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> (Jwt) context.getAuthentication().getPrincipal())
                .switchIfEmpty(Mono.error(new ExternalServiceException(ExceptionMessages.AUTHENTICATION_REQUIRED.getMessage())))
                .flatMap(jwt -> client.get().uri(builder -> builder.path(ApiPaths.PERSONS)
                                .queryParam(ApiParameters.IDS, ids.stream().map(String::valueOf).collect(Collectors.joining(",")))
                                .build())
                        .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                        .retrieve().bodyToFlux(PersonSummaryResponseDto.class).collectList())
                .timeout(timeout)
                .onErrorMap(error -> error instanceof WebClientException || error instanceof TimeoutException,
                        error -> new ExternalServiceException(ExceptionMessages.PERSON_DETAILS_UNAVAILABLE.getMessage()))
                .map(results -> results.stream().map(person -> new PersonSummary(person.getId(),
                                person.getName(), person.getEmail()))
                        .collect(Collectors.toMap(PersonSummary::getId, Function.identity())));
    }
}
