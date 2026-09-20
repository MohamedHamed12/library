package io.pillopl.library.lending.patron.infrastructure;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.FindAvailableBook;
import io.pillopl.library.lending.book.FindBookOnHold;
import io.pillopl.library.lending.patron.application.checkout.CheckingOutBookOnHold;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.ExtendingHold;
import io.pillopl.library.lending.patron.application.hold.HandleDuplicateHold;
import io.pillopl.library.lending.patron.application.hold.PlacingOnHold;
import io.pillopl.library.lending.patron.application.patron.PatronIdGenerator;
import io.pillopl.library.lending.patron.application.patron.ReactivatingPatron;
import io.pillopl.library.lending.patron.application.patron.RegisteringPatron;
import io.pillopl.library.lending.patron.application.patron.SuspendingPatron;
import io.pillopl.library.lending.patron.model.PatronFactory;
import io.pillopl.library.lending.patron.model.Patrons;

@Configuration
@EnableJdbcRepositories
public class PatronConfiguration {

  @Bean
  CheckingOutBookOnHold checkingOutBookOnHold(
      FindBookOnHold findBookOnHold, Patrons patronRepository) {
    return new CheckingOutBookOnHold(findBookOnHold, patronRepository);
  }

  @Bean
  CancelingHold cancelingHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
    return new CancelingHold(findBookOnHold, patronRepository);
  }

  @Bean
  ExtendingHold extendingHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
    return new ExtendingHold(findBookOnHold, patronRepository);
  }

  @Bean
  HandleDuplicateHold handleDuplicateHold(CancelingHold cancelingHold, Clock clock) {
    return new HandleDuplicateHold(cancelingHold, clock);
  }

  @Bean
  PlacingOnHold placingOnHold(FindAvailableBook findAvailableBook, Patrons patronRepository) {
    return new PlacingOnHold(findAvailableBook, patronRepository);
  }

  @Bean
  RegisteringPatron registeringPatron(
      PatronIdGenerator patronIdGenerator, Patrons patronRepository) {
    return new RegisteringPatron(patronIdGenerator, patronRepository);
  }

  @Bean
  SuspendingPatron suspendingPatron(Patrons patronRepository) {
    return new SuspendingPatron(patronRepository);
  }

  @Bean
  ReactivatingPatron reactivatingPatron(Patrons patronRepository) {
    return new ReactivatingPatron(patronRepository);
  }

  @Bean
  PatronIdGenerator patronIdGenerator() {
    return new RandomPatronIdGenerator();
  }

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  Patrons patronRepository(
      PatronEntityRepository patronEntityRepository, DomainEvents domainEvents) {
    return new PatronsDatabaseRepository(
        patronEntityRepository, new DomainModelMapper(new PatronFactory()), domainEvents);
  }
}
