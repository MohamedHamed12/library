package io.pillopl.library.lending.dailysheet.model;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.lending.patron.model.PatronEvent;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

public record HoldsToExpireSheet(List<ExpiredHold> expiredHolds) {

  public HoldsToExpireSheet {
    Objects.requireNonNull(expiredHolds, "expiredHolds");
  }

  public List<ExpiredHold> getExpiredHolds() {
    return expiredHolds;
  }

  public Stream<PatronEvent.BookHoldExpired> toStreamOfEvents(Instant processingTime) {
    return expiredHolds.toStream().map(expiredHold -> expiredHold.toEvent(processingTime));
  }

  public int count() {
    return expiredHolds.size();
  }
}
