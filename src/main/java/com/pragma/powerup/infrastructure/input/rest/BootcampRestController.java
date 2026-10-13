package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.BootcampRequestDto;
import com.pragma.powerup.application.dto.response.BootcampResponseDto;
import com.pragma.powerup.application.dto.request.BootcampPageRequestDto;
import com.pragma.powerup.application.dto.response.BootcampPageResponseDto;
import com.pragma.powerup.application.handler.IBootcampListHandler;
import com.pragma.powerup.application.handler.IBootcampHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final IBootcampListHandler listHandler;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBootcamp(@PathVariable Long id) {
        return handler.deleteBootcamp(id);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BootcampPageResponseDto> list(@Valid @ModelAttribute BootcampPageRequestDto request) {
        return listHandler.list(request);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BootcampResponseDto> createBootcamp(@Valid @RequestBody BootcampRequestDto request) {
        return handler.createBootcamp(request);
    }
}
