package com.productos.crud.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @Pattern(regexp = "^(?!\\s*$).+", message = "The value must not be blank when provided.")
        @Size(min = 2, max = 100, message = "The length must be between 2 and 100 characters.")
        String name,

        @DecimalMin(value = "0.01", message = "Price must be at least 0.01.")
        @Digits(integer = 8, fraction = 2, message = "Price must have up to 8 digits before the decimal point and up to 2 digits after the decimal point.")
        BigDecimal price
) {}
