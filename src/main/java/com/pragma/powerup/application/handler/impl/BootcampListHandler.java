package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.BootcampPageRequestDto;
import com.pragma.powerup.application.dto.response.BootcampPageResponseDto;
import com.pragma.powerup.application.handler.IBootcampListHandler;
import com.pragma.powerup.application.mapper.BootcampPageMapper;
import com.pragma.powerup.domain.api.IBootcampListServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BootcampListHandler implements IBootcampListHandler {
    private final IBootcampListServicePort servicePort;
    private final BootcampPageMapper mapper;

    @Override
    public Mono<BootcampPageResponseDto> list(BootcampPageRequestDto request) {
        return servicePort.list(mapper.toQuery(request)).map(mapper::toResponse);
    }
}
