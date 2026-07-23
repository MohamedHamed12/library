package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.application.checkout.CheckingOutBookOnHold;
import io.pillopl.library.lending.patron.application.checkout.RegisteringOverdueCheckout;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.ExpiringHolds;
import io.pillopl.library.lending.patron.application.hold.FindAvailableBook;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.application.hold.HandleDuplicateHold;
import io.pillopl.library.lending.patron.application.hold.PlacingOnHold;
import io.pillopl.library.lending.patron.model.PatronFactory;
import io.pillopl.library.lending.patron.model.Patrons;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import java.time.Clock;

@Configuration
@EnableJdbcRepositories
public class PatronConfiguration {

    @Bean
    CheckingOutBookOnHold checkingOutBookOnHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
        return new CheckingOutBookOnHold(findBookOnHold, patronRepository);
    }

    @Bean
    RegisteringOverdueCheckout registeringOverdueCheckout(DailySheet dailySheet, Patrons patronRepository, Clock clock) {
        return new RegisteringOverdueCheckout(dailySheet, patronRepository, clock);
    }

    @Bean
    CancelingHold cancelingHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
        return new CancelingHold(findBookOnHold, patronRepository);
    }

    @Bean
    ExpiringHolds expiringHolds(DailySheet dailySheet, Patrons patronRepository, Clock clock) {
        return new ExpiringHolds(dailySheet, patronRepository, clock);
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
    Patrons patronRepository(PatronEntityRepository patronEntityRepository,
                             DomainEvents domainEvents) {
        return new PatronsDatabaseRepository(
                patronEntityRepository,
                new DomainModelMapper(new PatronFactory()),
                domainEvents);
    }
}
