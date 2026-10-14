package com.pragma.powerup.infrastructure.security;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.infrastructure.exceptionhandler.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class SecurityErrorHandler implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

    private final JsonMapper mapper;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {
        exchange.getResponse().getHeaders().set("WWW-Authenticate", "Bearer");
        return write(exchange, HttpStatus.UNAUTHORIZED, ExceptionMessages.AUTHENTICATION_REQUIRED);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException exception) {
        return write(exchange, HttpStatus.FORBIDDEN, ExceptionMessages.ACCESS_DENIED);
    }

    private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, ExceptionMessages message) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = mapper.writeValueAsBytes(ErrorResponse.from(message.getMessage()));
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }
}
