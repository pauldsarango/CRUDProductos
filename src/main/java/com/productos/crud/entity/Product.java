package com.productos.crud.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "productos")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Column(nullable = false, length = 100, updatable = false, unique = true)
    @EqualsAndHashCode.Include
    private String sku;

    @Version
    private Long version;

    public  Product(String sku, String name, BigDecimal price) {
        if(sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku cannot be null or blank");
        }
        this.sku = sku;
        changeName(name);
        changePrice(price);
    }

    // cambiar nombre con validaciones
    public void changeName(String newName) {
        if(newName == null || newName.isBlank()){
            throw new IllegalArgumentException("newName cannot be null or blank");
        }
        this.name = newName;
    }

    // cambiar precio con validaciones
    public void changePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() <= 0) {
            throw new IllegalArgumentException("newPrice must be greater than zero");
        }
        this.price = newPrice;
    }
}
