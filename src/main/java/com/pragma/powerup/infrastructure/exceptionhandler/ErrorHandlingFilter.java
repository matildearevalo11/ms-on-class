package com.pragma.powerup.infrastructure.exceptionhandler;

import com.pragma.powerup.domain.exception.ConflictException;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DuplicateKeyException;
import com.pragma.powerup.infrastructure.input.rest.support.RequestValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ErrorHandlingFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return Mono.defer(() -> next.handle(request)).onErrorResume(this::toResponse);
    }

    private Mono<ServerResponse> toResponse(Throwable error) {
        return switch (error) {
            case ConflictException exception -> respond(HttpStatus.CONFLICT, exception.getMessage());
            case DuplicateKeyException ignored -> respond(HttpStatus.CONFLICT, ExceptionMessages.CONCURRENT_MODIFICATION.getMessage());
            case ConcurrencyFailureException ignored -> respond(HttpStatus.CONFLICT, ExceptionMessages.CONCURRENT_MODIFICATION.getMessage());
            case NotFoundException exception -> respond(HttpStatus.NOT_FOUND, exception.getMessage());
            case ValidationException exception -> respond(HttpStatus.BAD_REQUEST, exception.getMessage());
            case ExternalServiceException exception -> respond(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
            case RequestValidationException exception -> respond(HttpStatus.BAD_REQUEST, exception.getMessage());
            case ServerWebInputException ignored ->
                    respond(HttpStatus.BAD_REQUEST, ExceptionMessages.INVALID_REQUEST.getMessage());
            case ResponseStatusException exception ->
                    respond(exception.getStatusCode(), ExceptionMessages.INVALID_REQUEST.getMessage());
            case DataAccessException exception -> {
                log.error("Persistence operation failed", exception);
                yield respond(HttpStatus.SERVICE_UNAVAILABLE, ExceptionMessages.DATABASE_UNAVAILABLE.getMessage());
            }
            default -> {
                log.error("Unexpected request failure", error);
                yield respond(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.INTERNAL_ERROR.getMessage());
            }
        };
    }

    private Mono<ServerResponse> respond(HttpStatusCode status, String message) {
        return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ErrorResponse.from(message));
    }
}
