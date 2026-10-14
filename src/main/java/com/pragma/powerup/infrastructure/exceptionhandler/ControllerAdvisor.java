package com.pragma.powerup.infrastructure.exceptionhandler;

import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.bind.support.WebExchangeBindException;

@Slf4j
@RestControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(ConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler({DuplicateKeyException.class, ConcurrencyFailureException.class})
    public ResponseEntity<ErrorResponse> concurrentEnrollment(DataAccessException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.from(ExceptionMessages.ENROLLMENT_CONFLICT.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> requestValidation(WebExchangeBindException exception) {
        String message = exception.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst().orElse(ExceptionMessages.INVALID_REQUEST.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.from(message));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> validation(ValidationException exception) {
        return ResponseEntity.badRequest().body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> externalService(ExternalServiceException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> invalidRequest() {
        return ResponseEntity.badRequest().body(ErrorResponse.from(ExceptionMessages.INVALID_REQUEST.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> persistenceFailure(DataAccessException exception) {
        log.error("Persistence operation failed", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.from(ExceptionMessages.DATABASE_UNAVAILABLE.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpectedFailure(Exception exception) {
        log.error("Unexpected request failure", exception);
        return ResponseEntity.internalServerError().body(ErrorResponse.from(ExceptionMessages.INTERNAL_ERROR.getMessage()));
    }
}
