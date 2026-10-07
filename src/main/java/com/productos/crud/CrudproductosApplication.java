package com.productos.crud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CrudproductosApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudproductosApplication.class, args);
    }

}
