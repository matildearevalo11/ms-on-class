package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.BootcampRequestDto;
import com.pragma.powerup.application.dto.response.BootcampResponseDto;
import com.pragma.powerup.application.handler.IBootcampHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bootcamps")
@RequiredArgsConstructor
public class BootcampRestController {
    private final IBootcampHandler handler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BootcampResponseDto> createBootcamp(@Valid @RequestBody BootcampRequestDto request) {
        return handler.createBootcamp(request);
    }
}
