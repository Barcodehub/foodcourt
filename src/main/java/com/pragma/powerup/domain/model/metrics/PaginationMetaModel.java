package com.pragma.powerup.domain.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationMetaModel {
    private Integer page;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
}

