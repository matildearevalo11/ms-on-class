package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.BootcampRequestDto;
import com.pragma.powerup.application.dto.response.BootcampResponseDto;
import reactor.core.publisher.Mono;

public interface IBootcampHandler {
    Mono<BootcampResponseDto> createBootcamp(BootcampRequestDto request);
}
