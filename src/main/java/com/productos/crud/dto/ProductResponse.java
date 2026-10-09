package com.productos.crud.dto;


import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        Instant updatedAt
) {}
