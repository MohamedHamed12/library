package io.pillopl.library.commons.database;

import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

public final class DataSourceFactory {

    private DataSourceFactory() {
    }

    public static DataSource create(JdbcConnectionDetails connectionDetails) {
        if (connectionDetails == null) {
            return new EmbeddedDatabaseBuilder()
                    .generateUniqueName(true)
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(connectionDetails.getDriverClassName());
        dataSource.setUrl(connectionDetails.getJdbcUrl());
        dataSource.setUsername(connectionDetails.getUsername());
        dataSource.setPassword(connectionDetails.getPassword());
        return dataSource;
    }
}
