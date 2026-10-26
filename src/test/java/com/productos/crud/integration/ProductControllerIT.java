package com.productos.crud.integration;

import com.productos.crud.TestContainersConfiguration;
import com.productos.crud.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfiguration.class)
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private ProductRepository repository;

    private static final String BODY = """
            {"sku": "ZAP001NEG40", "name": "Zapatillas", "price": 49.99}
            """;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Carrera simulada: la BD rechaza el SKU duplicado y la API responde 409")
    void shouldReturn409WhenDatabaseConstraintRejectsDuplicateSku() throws Exception {
        // Arrange: primer producto, creado por la API real
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());

        // Simulamos la carrera: existsBySku "no ve" el producto existente
        doReturn(false).when(repository).existsBySku(anyString());

        // Act + Assert: ahora el rechazo viene de PostgreSQL, no de existsBySku
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A product with this SKU already exists"));
    }

    @Test
    @DisplayName("SKU duplicado detectado por existsBySku devuelve 409 con el SKU en el mensaje")
    void shouldReturn409WhenServiceDetectsDuplicateSku() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("ZAP001NEG40")));
    }
}


