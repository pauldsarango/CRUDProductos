package com.productos.crud.dto;

import java.util.Arrays;

public enum ProductSortField {
    ID("id"),
    SKU("sku"),
    PRICE("price"),
    CREATED_AT("createdAt"),   // Se usa el nombre real de la propiedad de fecha del Entity
    UPDATED_AT("updatedAT");

    private final String property;

    ProductSortField(String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }

    public static ProductSortField fromString(String value) {
        String v = value.trim();
        return Arrays.stream(values())
                .filter(f -> f.name().equalsIgnoreCase(v) || f.property().equalsIgnoreCase(v))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Campo de orden no permitido: " + value));
    }
}
