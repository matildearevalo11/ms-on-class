package com.pragma.powerup.domain.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BootcampPage {
    private final List<BootcampListItem> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final long totalPages;
}
