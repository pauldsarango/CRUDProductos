package com.productos.crud.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PaginationParams(
        @Min(value = 0, message = "page must be >= 0")
        Integer page,

        @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100")
        Integer size,

        ProductSortField sortBy,

        Sort.Direction direction


) {
    public PaginationParams {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (sortBy == null) sortBy = ProductSortField.ID;
        if (direction == null) direction = Sort.Direction.ASC;
    }

    public Pageable toPageable() {
        Sort sort = Sort.by(direction, sortBy.property());
        if (sortBy != ProductSortField.ID) {
            sort = sort.and(Sort.by(direction, ProductSortField.ID.property()));
        }
        return PageRequest.of(page, size, sort);
    }
}
