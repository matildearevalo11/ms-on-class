package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.BootcampPageRequestDto;
import com.pragma.powerup.application.dto.response.BootcampPageResponseDto;
import reactor.core.publisher.Mono;

public interface IBootcampListHandler {
    Mono<BootcampPageResponseDto> list(BootcampPageRequestDto request);
}
