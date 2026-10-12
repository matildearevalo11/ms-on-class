package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.BootcampRequestDto;
import com.pragma.powerup.application.dto.response.BootcampResponseDto;
import com.pragma.powerup.application.handler.IBootcampHandler;
import com.pragma.powerup.application.mapper.BootcampMapper;
import com.pragma.powerup.domain.api.IBootcampServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BootcampHandler implements IBootcampHandler {
    private final IBootcampServicePort servicePort;
    private final BootcampMapper mapper;

    @Override
    @Transactional
    public Mono<BootcampResponseDto> createBootcamp(BootcampRequestDto request) {
        return servicePort.createBootcamp(mapper.toModel(request)).map(mapper::toResponse);
    }
}
