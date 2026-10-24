package com.productos.crud.repository;

import com.productos.crud.config.JpaAuditingConfig;
import com.productos.crud.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;


import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;


@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(JpaAuditingConfig.class)
public class ProductRepositoryTest {
    @Autowired
    private ProductRepository repository;

    @Test
    @DisplayName("Guardar dos productos con el mismo SKU viola el constraint uk_product_sku")
    void shouldViolateUniqueConstraintWhenSkuIsDuplicated(){
        // Arrange
        Product product1 = new Product("SKU-001", "Camisa", new BigDecimal("25.99"));
        Product product2 = new Product("SKU-001", "Gorra", new BigDecimal("9.99"));

        repository.saveAndFlush(product1);

        // Act
        Throwable thrown = catchThrowable(() ->
                repository.saveAndFlush(product2));
        // Asserts
        assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(findConstraintName(thrown)).containsIgnoringCase("uk_product_sku");
    }

    @Test
    @DisplayName("Dos productos con SKU distinto se guardan sin violar nada")
    void shouldSaveProductsWithDifferentSku() {
        Product product1 = new Product("SKU-001", "Camisa", new BigDecimal("25.99"));
        Product product2 = new Product("SKU-002", "Gorra", new BigDecimal("9.99"));

        repository.saveAndFlush(product1);

        assertThatCode(() ->
                repository.saveAndFlush(product2))
                .doesNotThrowAnyException();
    }

    private String findConstraintName(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof org.hibernate.exception.ConstraintViolationException cve
                    && cve.getConstraintName() != null) {
                return cve.getConstraintName();
            }
            current = current.getCause();
        }
        return null;
    }
}
