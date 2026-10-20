package com.productos.crud.service;

import com.productos.crud.dto.CreateProductRequest;
import com.productos.crud.dto.PaginationParams;
import com.productos.crud.dto.ProductResponse;
import com.productos.crud.dto.ProductSortField;
import com.productos.crud.entity.Product;
import com.productos.crud.exception.DuplicateResourceException;
import com.productos.crud.exception.ResourceNotFoundException;
import com.productos.crud.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import static org.springframework.data.domain.Sort.Direction.ASC;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
    @DisplayName("Debe obtener todos los productos exitosamente")
    void shouldGetAllProductsSuccessfully(){
        // Arrange
        Product product1 = new Product("ZAP001NEG40",
                "Zapatillas Runner Negras",
                new BigDecimal("49.99")
        );
        Product product2 = new Product("CAM003BLA10",
                "Camisa Blanca",
                new BigDecimal("25.99")
        );
        // Agregamos un parametro a un campo privado, no persiste en la BD
        ReflectionTestUtils.setField(product1, "id", 10L);
        ReflectionTestUtils.setField(product2, "id", 20L);
        // Creamos la lista de productos
        List<Product> products = List.of(product1, product2);
        // indicamos los detalles de la paginación
        PaginationParams params = new PaginationParams(0, 10, ProductSortField.ID, ASC);
        // creamos una Page de productos
        Page<Product> productPage = new PageImpl<>(products,params.toPageable(),2);

        when(repository.findAll(params.toPageable())).thenReturn(productPage);

        // Act
        Page<ProductResponse> responses = service.getAllProducts(params);

        // Assert
        assertThat(responses.getTotalElements()).isEqualTo(productPage.getTotalElements());
        assertThat(responses.getTotalPages()).isEqualTo(productPage.getTotalPages());
        assertThat(responses.getNumberOfElements()).isEqualTo(productPage.getNumberOfElements());
        // Verificar contenido
        assertThat(responses.getContent()).hasSize(2);
        assertThat(responses.getContent())
                .extracting(ProductResponse::id)
                .containsExactly(10L, 20L);
        assertThat(responses.getContent())
                .extracting(ProductResponse::sku)
                .containsExactly("ZAP001NEG40", "CAM003BLA10");

        verify(repository, times(1)).findAll(params.toPageable());
    }

    @Test
    @DisplayName("Debe obtener el producto por ID exitosamente")
    void shouldGetProductByIdSuccessfully(){
        // Arrange
        long id = 4L;
        Product product = new Product("ZAP001NEG40",
                "Zapatillas Runner Negras",
                new BigDecimal("49.99")
        );
        // Agregamos un parametro a un campo privado, no persiste en la BD
        ReflectionTestUtils.setField(product, "id", id);

        when(repository.findById(id)).thenReturn(Optional.of(product));

        // Act
        ProductResponse response = service.getProductById(id);

        // Assert
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.sku()).isEqualTo("ZAP001NEG40");
        assertThat(response.name()).isEqualTo("Zapatillas Runner Negras");
        assertThat(response.price()).isEqualTo(new BigDecimal("49.99"));

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si no encuentra un producto por ID")
    void shouldThrowResourceNotFoundExceptionById(){
        // Arrange
        Long id = 4L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.getProductById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id " + id);

        verify(repository, times(1)).findById(4L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Debe obtener el producto por Sku exitosamente")
    void shouldGetProductBySkuSuccessfully(){
        // Arrange
        Product product = new Product("ZAP001NEG40",
                "Zapatillas Runner Negras",
                new BigDecimal("49.99")
        );
        // Agregamos un parametro a un campo privado, no persiste en la BD
        ReflectionTestUtils.setField(product, "id", 1L);

        when(repository.findBySku("ZAP001NEG40")).thenReturn(Optional.of(product));

        // Act
        ProductResponse response = service.getProductBySku("ZAP001NEG40");

        // Assert
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.sku()).isEqualTo("ZAP001NEG40");
        assertThat(response.name()).isEqualTo("Zapatillas Runner Negras");
        assertThat(response.price()).isEqualTo(new BigDecimal("49.99"));

        verify(repository, times(1)).findBySku("ZAP001NEG40");
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si no encuentra un producto por SKU")
    void shouldThrowResourceNotFoundExceptionBySku(){
        // Arrange
        String sku = "ZAP001NEG40";
        when(repository.findBySku(sku)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.getProductBySku(sku))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with sku " + sku);

        verify(repository, times(1)).findBySku(sku);
        verifyNoMoreInteractions(repository);
    }

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
        // Agregamos un parametro a un campo privado, no persiste en la BD
        ReflectionTestUtils.setField(product, "id", 1L);

        when(repository.existsBySku("ZAP001NEG40")).thenReturn(false);
        when(repository.save(any(Product.class))).thenReturn(product);

        // Act
        ProductResponse response = service.createProduct(request);     // ejecutamos el caso de uso

        // Assert  entrada al repositorio
        ArgumentCaptor<Product> captorProduct = ArgumentCaptor.forClass(Product.class); // capturamos lo que pasó el save
        verify(repository, times(1)).save(captorProduct.capture());
        Product capturedProduct = captorProduct.getValue();
        assertThat(capturedProduct.getId()).isNull();
        assertThat(capturedProduct.getSku()).isEqualTo("ZAP001NEG40");
        assertThat(capturedProduct.getName()).isEqualTo("Zapatillas Runner Negras");
        assertThat(capturedProduct.getPrice()).isEqualTo(new BigDecimal("49.99"));
        assertThat(capturedProduct.getUpdatedAt()).isNull();

        // Assert salida del Service: Product -> ProductResponse
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.sku()).isEqualTo("ZAP001NEG40");
        assertThat(response.name()).isEqualTo("Zapatillas Runner Negras");
        assertThat(response.price()).isEqualTo(new BigDecimal("49.99"));
        assertThat(response.updatedAt()).isNull();


        verify(repository).existsBySku("ZAP001NEG40");
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
                .hasMessageContaining("This SKU is already registered " + request.sku());

        verify(repository,times(1)).existsBySku("ZAP001NEG40");
        verify(repository, never()).save(any(Product.class));
        verifyNoMoreInteractions(repository);
    }
}
