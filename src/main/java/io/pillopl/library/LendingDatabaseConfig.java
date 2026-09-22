package io.pillopl.library;

import static io.pillopl.library.catalogue.BookType.Circulating;
import static io.pillopl.library.lending.patron.model.PatronType.Regular;

import java.time.Clock;
import java.util.UUID;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.commons.database.DataSourceFactory;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookInformation;
import io.pillopl.library.lending.book.model.BookRepository;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.EmailAddress;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;

@Configuration
@EntityScan(basePackages = "io.pillopl.library.lending")
@EnableJpaRepositories(basePackages = "io.pillopl.library.lending")
class LendingDatabaseConfig {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(LendingDatabaseConfig.class);

  @Bean(initMethod = "migrate")
  Flyway lendingFlyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration/lending")
        .load();
  }

  @Bean
  DataSource dataSource(ObjectProvider<JdbcConnectionDetails> connectionDetails) {
    return DataSourceFactory.create(connectionDetails.getIfAvailable());
  }

  @Profile("local")
  @Bean
  CommandLineRunner init(BookRepository bookRepository, Patrons patrons, Clock clock) {
    return args -> {
      UUID bookId = UUID.randomUUID();
      UUID libraryBranchId = UUID.randomUUID();
      UUID patronId = UUID.randomUUID();

      AvailableBook availableBook =
          new AvailableBook(
              new BookInformation(new BookId(bookId), Circulating),
              new LibraryBranchId(libraryBranchId),
              new Version(0));
      bookRepository.save(availableBook);
      patrons.publish(
          PatronCreated.createdAt(
              clock.instant(),
              new PatronId(patronId),
              Regular,
              EmailAddress.of("local-patron@example.test")));

      log.info("Created bookId: {}", bookId);
      log.info("Created libraryBranchId: {}", libraryBranchId);
      log.info("Created patronId: {}", patronId);
    };
  }
}
