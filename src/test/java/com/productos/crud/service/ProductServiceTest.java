package com.productos.crud.service;

import com.productos.crud.dto.CreateProductRequest;
import com.productos.crud.dto.ProductResponse;
import com.productos.crud.entity.Product;
import com.productos.crud.exception.DuplicateResourceException;
import com.productos.crud.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        ProductResponse response = service.createProduct(request);

        // Asserts
        assertThat(response.id()).isNull();
        assertThat(response.sku()).isEqualTo("ZAP001NEG40");
        assertThat(response.name()).isEqualTo("Zapatillas Runner Negras");
        assertThat(response.price()).isEqualTo(new  BigDecimal("49.99"));
        assertThat(response.updatedAt()).isNull();

        verify(repository,times(1)).existsBySku("ZAP001NEG40");
        verify(repository,times(1)).save(any(Product.class));
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
