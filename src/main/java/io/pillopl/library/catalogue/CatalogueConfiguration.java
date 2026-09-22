package io.pillopl.library.catalogue;

import java.time.Clock;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.commons.events.publisher.DomainEventsConfig;

@Configuration
@EnableAutoConfiguration
@Import({CatalogueDatabaseConfig.class, DomainEventsConfig.class})
public class CatalogueConfiguration {

  @Bean
  Catalogue catalogue(CatalogueDatabase catalogueDatabase, DomainEvents domainEvents, Clock clock) {
    return new Catalogue(catalogueDatabase, domainEvents, clock);
  }

  @Bean
  CatalogueDatabase catalogueDatabase(
      CatalogueBookJpaRepository bookRepository,
      CatalogueBookInstanceJpaRepository bookInstanceRepository) {
    return new CatalogueDatabase(bookRepository, bookInstanceRepository);
  }

  @Profile("local")
  @Bean
  CommandLineRunner init(Catalogue catalogue) {
    return args -> {
      catalogue.addBook("Joshua Bloch", "Effective Java", "0321125215");
      catalogue.addBookInstance("0321125215", BookType.Restricted);
    };
  }
}
