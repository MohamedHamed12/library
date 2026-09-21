package io.pillopl.library.lending.dailysheet.application;

import java.time.Clock;
import java.time.Instant;

import io.pillopl.library.commons.commands.BatchResult;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.patron.model.Patrons;

public class RegisteringOverdueCheckout {

  private final DailySheet find;
  private final Patrons patronRepository;
  private final Clock clock;

  public RegisteringOverdueCheckout(DailySheet find, Patrons patronRepository, Clock clock) {
    this.find = find;
    this.patronRepository = patronRepository;
    this.clock = clock;
  }

  public BatchResult registerOverdueCheckouts() {
    Instant processingTime = clock.instant();
    boolean someFailed =
        find.queryForCheckoutsToOverdue(processingTime)
            .toStreamOfEvents(processingTime)
            .map(this::publish)
            .anyMatch(success -> !success);
    return someFailed ? BatchResult.SomeFailed : BatchResult.FullSuccess;
  }

  private boolean publish(OverdueCheckoutRegistered event) {
    try {
      patronRepository.publish(event);
      return true;
    } catch (RuntimeException exception) {
      return false;
    }
  }
}
