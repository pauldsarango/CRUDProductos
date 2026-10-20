package com.productos.crud.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Convierte el parámetro "direction" de la request en un Sort.Direction,
 * aceptando asc/ASC/Asc y desc/DESC/Desc.
 * Si el valor no es válido lanza IllegalArgumentException y Spring responde 400.
 */
@Component
public class StringToSortDirectionConverter implements Converter<String, Sort.Direction> {

    @Override
    public Sort.Direction convert(String source) {
        return Sort.Direction.fromString(source.trim());
    }
}