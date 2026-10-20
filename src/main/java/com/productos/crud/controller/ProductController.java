package com.productos.crud.controller;

import com.productos.crud.dto.CreateProductRequest;
import com.productos.crud.dto.PaginationParams;
import com.productos.crud.dto.ProductResponse;
import com.productos.crud.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {this.service = service;}

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Valid
            @ModelAttribute
            PaginationParams params
    ) {
        Page<ProductResponse> allProducts = service.getAllProducts(params);
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Positive(message = "must be greater than zero")
            @NotNull(message = "must not be null")
            @PathVariable Long id
    ){
        ProductResponse product = service.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(
            @NotBlank(message = "SKU cannot be blank")
            @Size(min = 4, max = 50, message = "SKU must be between {min} and {max} characters")
            @PathVariable String sku
    ){
        ProductResponse product = service.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
            ){
        ProductResponse product = service.createProduct(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.id())
                .toUri();
        return ResponseEntity.created(location).body(product);
    }

}
