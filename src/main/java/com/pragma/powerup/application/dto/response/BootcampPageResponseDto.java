package com.pragma.powerup.application.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BootcampPageResponseDto {
    private final List<BootcampListItemDto> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final long totalPages;
}
