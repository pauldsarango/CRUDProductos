package com.productos.crud.controller;

import com.productos.crud.dto.CreateProductRequest;
import com.productos.crud.dto.ProductResponse;
import com.productos.crud.exception.DuplicateResourceException;
import com.productos.crud.exception.ResourceNotFoundException;
import com.productos.crud.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;
    @Autowired
    private ObjectMapper objectMapper;



    // TEST DE ENTRADAS GET
    @Test
    @DisplayName("GET /api/products/{id} - Negative Id - Debe retornar 400 Bad Request")
    void shouldReturn400WhenIdIsNegative() throws Exception {
        // Arrange
        Long id = -1L;
        // Act
        mockMvc.perform(get("/api/products/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter"))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.validationErrors", aMapWithSize(1)))
                .andExpect(jsonPath("$.validationErrors.id", hasItem("must be greater than zero")));

        verifyNoInteractions(service);
    }
    @Test
    @DisplayName("GET /api/products/{id} - Debe retornar 400 Bad Request")
    void shouldReturn400WhenIdIsNotNumeric() throws Exception {
        // Arrange
        String badId = "abc";
        // Act
        mockMvc.perform(get("/api/products/{id}", badId)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parameter 'id' has an invalid value: 'abc'. Expected type is 'Long'"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));


        verifyNoInteractions(service);
    }
    @Test
    @DisplayName("GET /api/products/sku/{sku} - Debe retornar 400 Bad Request")
    void shouldReturn400WhenSkuIsTooShort() throws Exception {
        // Arrange
        String sku = "abc";
        // Act
        mockMvc.perform(get("/api/products/sku/{sku}", sku)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter"))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.validationErrors", aMapWithSize(1)))
                .andExpect(jsonPath("$.validationErrors.sku", hasItem("SKU must be between 4 and 50 characters")));

        verifyNoInteractions(service);
    }

    // TEST GET
    @Test
    @DisplayName("GET /api/products/{id} - Debe obtener un producto y retornar un 200 OK")
    void shouldGetProductByIdSuccessfully() throws Exception {
        // Arrange
        Long id = 1L;
        ProductResponse response = new ProductResponse(
                id,
                "Zapatillas Runner Negras",
                "ZAP001NEG40",
                new BigDecimal("49.99"),
                Instant.now()
        );

        when(service.getProductById(id)).thenReturn(response);

        // Act
        mockMvc.perform(get("/api/products/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert salida a cliente
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("ZAP001NEG40"))
                .andExpect(jsonPath("$.name").value("Zapatillas Runner Negras"))
                .andExpect(jsonPath("$.price").value(49.99))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        verify(service, times(1)).getProductById(id);
    }
    @Test
    @DisplayName("GET /api/products/{id} - Debe retornar 404 Not Found")
    void shouldReturn404WhenProductDoesNotExistById() throws Exception {
        // Arrange
        Long id = 99L;
        when(service.getProductById(id))
                .thenThrow(new ResourceNotFoundException("Product not found with id " + id));

        // Act
        mockMvc.perform(get("/api/products/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                // Asserts
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id " + id))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        verify(service, times(1)).getProductById(id);

    }

    @Test
    @DisplayName("GET /api/products/sku/{sku} - Debe obtener un producto y retornar un 200 OK")
    void shouldGetProductBySkuSuccessfully() throws Exception {
        // Arrange
        String sku = "ZAP001NEG40";
        ProductResponse response = new ProductResponse(
                1L,
                "Zapatillas Runner Negras",
                "ZAP001NEG40",
                new BigDecimal("49.99"),
                Instant.now()
        );

        when(service.getProductBySku(sku)).thenReturn(response);

        // Act
        mockMvc.perform(get("/api/products/sku/{sku}", sku)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert salida a cliente
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("ZAP001NEG40"))
                .andExpect(jsonPath("$.name").value("Zapatillas Runner Negras"))
                .andExpect(jsonPath("$.price").value(49.99))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        verify(service, times(1)).getProductBySku(sku);
    }

    @Test
    @DisplayName("GET /api/products/sku/{sku} - Debe retornar 404 Not Found")
    void shouldReturn404WhenProductDoesNotExistBySku() throws Exception {
        // Arrange
        String sku = "nonExistentSku";
        when(service.getProductBySku(sku))
                .thenThrow(new ResourceNotFoundException("Product not found with sku " + sku));

        // Act
        mockMvc.perform(get("/api/products/sku/{sku}", sku)
                        .accept(MediaType.APPLICATION_JSON))
                // Asserts
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with sku " + sku))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        verify(service, times(1)).getProductBySku(sku);

    }

    // TEST DE ENTRADA POST
    @Test
    @DisplayName("POST /api/products - Debe retornar 400 Bad Request")
    void shouldReturn400WhenMultipleFieldsAreInvalid() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "",
                "",
                new BigDecimal("-999999999999.9999999")
        );
        // Act
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.validationErrors", aMapWithSize(3)))
                .andExpect(jsonPath("$.validationErrors.sku", hasItem("Sku cannot be null or blank.")))
                .andExpect(jsonPath("$.validationErrors.name", hasItem("Name cannot be null or blank.")))
                .andExpect(jsonPath("$.validationErrors.price", hasItems(
                        "Price must be at least 0.01."
                        ,"Price must have up to 8 digits before the decimal point and up to 2 digits after the decimal point.")));

        verifyNoMoreInteractions(service);
    }

    // TEST POST
    @Test
    @DisplayName("POST /api/products - Debe crear un producto y retornar 201 Created")
    void shouldCreateProduct() throws Exception{
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "ZAP001NEG40",
                "Zapatillas Runner Negras",
                new BigDecimal("49.99")
        );
        ProductResponse response = new ProductResponse(
                1L,
                "Zapatillas Runner Negras",
                "ZAP001NEG40",
                new BigDecimal("49.99"),
                Instant.now()
        );

        when(service.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        // Act
        mockMvc.perform(post("/api/products")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                // Assert salida a cliente
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/products/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("ZAP001NEG40"))
                .andExpect(jsonPath("$.name").value("Zapatillas Runner Negras"))
                .andExpect(jsonPath("$.price").value(49.99));

        // Assert  entradas al service
        ArgumentCaptor<CreateProductRequest> captorProduct = ArgumentCaptor.forClass(CreateProductRequest.class); // capturamos lo que pasó el service
        verify(service, times(1)).createProduct(captorProduct.capture());
        CreateProductRequest capturedProduct = captorProduct.getValue();
        assertThat(capturedProduct.sku()).isEqualTo("ZAP001NEG40");
        assertThat(capturedProduct.name()).isEqualTo("Zapatillas Runner Negras");
        assertThat(capturedProduct.price()).isEqualTo(new BigDecimal("49.99"));

        verifyNoMoreInteractions(service);

    }

    @Test
    @DisplayName("POST /api/products - Debe lanzar DuplicateResourceException y retornar 409 Conflict")
    void shouldReturn409WhenSkuAlreadyExists() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "ZAP001NEG40",
                "Zapatillas Runner Negras",
                new BigDecimal("49.99")
        );

        when(service.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new DuplicateResourceException("This SKU is already registered " + request.sku()));

        // Act
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Asserts
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("This SKU is already registered " + request.sku()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        verify(service, times(1)).createProduct(any(CreateProductRequest.class));

    }


}
