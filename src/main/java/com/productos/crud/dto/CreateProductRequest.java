package com.productos.crud.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Name cannot be null or blank.")
        @Size(min = 2, max = 100, message = "The length must be between 2 and 100 characters.")
        String name,

         @NotNull(message = "Price cannot be null.")
         @DecimalMin(value = "0.01", message = "Price must be at least 0.01.")
         @Digits(integer = 8, fraction = 2, message = "Price must have up to 8 digits before the decimal point and up to 2 digits after the decimal point.")
        BigDecimal price,

        @NotBlank(message = "Sku cannot be null or blank.")
        @Size(min = 4, max = 50, message = "The length must be between 4 and 50 characters.")
        String sku
){}
