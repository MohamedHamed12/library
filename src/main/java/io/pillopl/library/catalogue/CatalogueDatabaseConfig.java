package io.pillopl.library.catalogue;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.pillopl.library.commons.database.DataSourceFactory;

@Configuration
@EntityScan(basePackageClasses = CatalogueBookEntity.class)
@EnableJpaRepositories(basePackageClasses = CatalogueBookJpaRepository.class)
class CatalogueDatabaseConfig {

  @Bean(initMethod = "migrate")
  Flyway catalogueFlyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration/catalogue")
        .load();
  }

  @Bean
  DataSource dataSource(ObjectProvider<JdbcConnectionDetails> connectionDetails) {
    return DataSourceFactory.create(connectionDetails.getIfAvailable());
  }
}
