package com.productos.crud.service;

import com.productos.crud.dto.CreateProductRequest;
import com.productos.crud.entity.Product;
import com.productos.crud.exception.DuplicateResourceException;
import com.productos.crud.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
     @DisplayName("Debe crear un producto exitosamente")
    void shouldCreateProductSuccessfully(){
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "ZAP001NEG40",
                "Zapatillas Runner Negras",
                new BigDecimal("49.99")
        );
        Product product = new Product("ZAP001NEG40",
                "Zapatillas Runner Negras",
                new BigDecimal("49.99")
        );

        when(repository.existsBySku("ZAP001NEG40")).thenReturn(false);
        when(repository.save(any(Product.class))).thenReturn(product);

        // Act
        service.createProduct(request);     // ejecutamos el caso de uso

        // Asserts
        ArgumentCaptor<Product> captorProduct = ArgumentCaptor.forClass(Product.class);         // capturamos lo que pasó el save
        verify(repository, times(1)).save(captorProduct.capture());

        Product capturedProduct = captorProduct.getValue();

        assertThat(capturedProduct.getId()).isNull();
        assertThat(capturedProduct.getSku()).isEqualTo("ZAP001NEG40");
        assertThat(capturedProduct.getName()).isEqualTo("Zapatillas Runner Negras");
        assertThat(capturedProduct.getPrice()).isEqualTo(new BigDecimal("49.99"));
        assertThat(capturedProduct.getUpdatedAt()).isNull();

        verify(repository,times(1)).existsBySku("ZAP001NEG40");
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException si SKU ya fué registrado")
    void shouldThrowExceptionWhenSkuAlreadyRegistered(){
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "ZAP001NEG40",
                "Zapatillas Runner Negras",
                new BigDecimal("49.99")
        );

        when(repository.existsBySku("ZAP001NEG40")).thenReturn(true);

        // Act + Asserts
        assertThatThrownBy(() -> service.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("This SKU is already registered: " + request.sku());

        verify(repository,times(1)).existsBySku("ZAP001NEG40");
        verify(repository, never()).save(any(Product.class));
        verifyNoMoreInteractions(repository);
    }
}
