package io.pillopl.library.database;

import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class PostgreSQLTestConfiguration {

    private static final String POSTGRESQL_IMAGE = "postgres:18-alpine";

    @Bean
    @ServiceConnection(type = JdbcConnectionDetails.class)
    PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer(POSTGRESQL_IMAGE)
                .withDatabaseName("library")
                .withUsername("library")
                .withPassword("library");
    }
}
