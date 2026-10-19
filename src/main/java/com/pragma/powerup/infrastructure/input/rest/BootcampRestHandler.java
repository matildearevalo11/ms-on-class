package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.BootcampPageRequestDto;
import com.pragma.powerup.application.dto.request.BootcampRequestDto;
import com.pragma.powerup.application.handler.IBootcampHandler;
import com.pragma.powerup.application.handler.IBootcampListHandler;
import com.pragma.powerup.application.handler.IBootcampPopularityHandler;
import com.pragma.powerup.application.handler.IEnrollmentHandler;
import com.pragma.powerup.infrastructure.constants.ApiParameters;
import com.pragma.powerup.infrastructure.input.rest.support.QueryParams;
import com.pragma.powerup.infrastructure.input.rest.support.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BootcampRestHandler {
    private final IBootcampHandler handler;
    private final IBootcampListHandler listHandler;
    private final IEnrollmentHandler enrollmentHandler;
    private final IBootcampPopularityHandler popularityHandler;
    private final RequestValidator validator;

    public Mono<ServerResponse> createBootcamp(ServerRequest request) {
        return validator.body(request, BootcampRequestDto.class)
                .flatMap(handler::createBootcamp)
                .flatMap(bootcamp -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON).bodyValue(bootcamp));
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        return Mono.fromCallable(() -> {
                    var query = new BootcampPageRequestDto();
                    query.setPage(QueryParams.integer(request, ApiParameters.PAGE, query.getPage()));
                    query.setSize(QueryParams.integer(request, ApiParameters.SIZE, query.getSize()));
                    query.setSortBy(QueryParams.text(request, ApiParameters.SORT_BY, query.getSortBy()));
                    query.setDirection(QueryParams.text(request, ApiParameters.DIRECTION, query.getDirection()));
                    return query;
                })
                .flatMap(validator::validate)
                .flatMap(listHandler::list)
                .flatMap(page -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(page));
    }

    public Mono<ServerResponse> findMostEnrolled(ServerRequest request) {
        return popularityHandler.findMostEnrolled()
                .flatMap(bootcamp -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(bootcamp));
    }

    public Mono<ServerResponse> deleteBootcamp(ServerRequest request) {
        return Mono.fromCallable(() -> QueryParams.pathId(request, ApiParameters.ID))
                .flatMap(handler::deleteBootcamp)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> enroll(ServerRequest request) {
        return request.principal().cast(JwtAuthenticationToken.class)
                .map(authentication -> Long.valueOf(authentication.getToken().getSubject()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .flatMap(personId -> enrollmentHandler.enroll(personId, QueryParams.pathId(request, ApiParameters.ID)))
                .then(ServerResponse.status(HttpStatus.CREATED).build());
    }
}
