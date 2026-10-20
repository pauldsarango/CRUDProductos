package com.productos.crud.config.converter;

import com.productos.crud.dto.ProductSortField;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Convierte el parámetro "sortBy" de la request en un ProductSortField,
 * aceptando mayúsculas o minúsculas (price, PRICE, createdAt, CREATED_AT).
 * Si el valor no es válido lanza IllegalArgumentException y Spring responde 400.
 */
@Component
public class StringToProductSortFieldConverter implements Converter<String, ProductSortField> {

    @Override
    public ProductSortField convert(@NonNull String source) {
        return ProductSortField.fromString(source);
    }
}
