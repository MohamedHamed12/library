package io.pillopl.library.lending.dailysheet.infrastructure;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.pillopl.library.lending.dailysheet.application.ExpiringHolds;
import io.pillopl.library.lending.dailysheet.application.RegisteringOverdueCheckout;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.model.Patrons;

@Configuration
public class DailySheetConfiguration {

  @Bean
  DailySheet sheetsReadModel(HoldSheetJpaRepository holds, CheckoutSheetJpaRepository checkouts) {
    return new SheetsReadModel(holds, checkouts);
  }

  @Bean
  RegisteringOverdueCheckout registeringOverdueCheckout(
      DailySheet dailySheet, Patrons patronRepository, Clock clock) {
    return new RegisteringOverdueCheckout(dailySheet, patronRepository, clock);
  }

  @Bean
  ExpiringHolds expiringHolds(DailySheet dailySheet, Patrons patronRepository, Clock clock) {
    return new ExpiringHolds(dailySheet, patronRepository, clock);
  }
}
