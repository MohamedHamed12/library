package io.pillopl.library.lending.dailysheet.model;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

public record CheckoutsToOverdueSheet(List<OverdueCheckout> checkouts) {

  public CheckoutsToOverdueSheet {
    Objects.requireNonNull(checkouts, "checkouts");
  }

  public List<OverdueCheckout> getCheckouts() {
    return checkouts;
  }

  public Stream<OverdueCheckoutRegistered> toStreamOfEvents(Instant processingTime) {
    return checkouts.toStream().map(checkout -> checkout.toEvent(processingTime));
  }

  public int count() {
    return checkouts.size();
  }
}
