package com.productos.crud.mapper;

import com.productos.crud.dto.CreateProductRequest;
import com.productos.crud.dto.ProductResponse;
import com.productos.crud.entity.Product;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getUpdatedAt()
        );
    }

    public static Product toProductEntity(CreateProductRequest request) {
        return new Product(
                request.sku(),
                request.name(),
                request.price()
        );
    }
}
