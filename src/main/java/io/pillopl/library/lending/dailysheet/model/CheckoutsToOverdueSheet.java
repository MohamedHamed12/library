package io.pillopl.library.lending.dailysheet.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;

public record CheckoutsToOverdueSheet(List<OverdueCheckout> checkouts) {

  public CheckoutsToOverdueSheet {
    Objects.requireNonNull(checkouts, "checkouts");
  }

  public List<OverdueCheckout> getCheckouts() {
    return checkouts;
  }

  public Stream<OverdueCheckoutRegistered> toStreamOfEvents(Instant processingTime) {
    return checkouts.stream().map(checkout -> checkout.toEvent(processingTime));
  }

  public int count() {
    return checkouts.size();
  }
}
