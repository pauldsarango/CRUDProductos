package com.productos.crud.service;

import com.productos.crud.dto.CreateProductRequest;
import com.productos.crud.dto.ProductResponse;
import com.productos.crud.entity.Product;
import com.productos.crud.exception.DuplicateResourceException;
import com.productos.crud.exception.ResourceNotFoundException;
import com.productos.crud.mapper.ProductMapper;
import com.productos.crud.repository.ProductRepository;
import org.springframework.stereotype.Service;


@Service
public class ProductService{
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public ProductResponse getProductById(Long id){
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        return ProductMapper.toProductResponse(product);
    }

    public ProductResponse getProductBySku(String sku){
        Product product = repository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with sku " + sku));
        return ProductMapper.toProductResponse(product);
    }

    public ProductResponse createProduct(CreateProductRequest request){
        if(repository.existsBySku(request.sku())){
            throw new DuplicateResourceException("This SKU is already registered " + request.sku());
        }
        Product product = ProductMapper.toProductEntity(request);
        product = repository.save(product);
        return ProductMapper.toProductResponse(product);
    }



}
