package com.productos.crud;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;

@TestConfiguration(proxyBeanMethods = false)

public class TestContainersConfiguration {
    @Bean
    @ServiceConnection
    public JdbcDatabaseContainer<?> postgresContainer() {
        return new PostgreSQLContainer("postgres:18-alpine");
    }
}
