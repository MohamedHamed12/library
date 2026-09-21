package io.pillopl.library.lending.dailysheet.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import io.pillopl.library.lending.patron.model.PatronEvent;

public record HoldsToExpireSheet(List<ExpiredHold> expiredHolds) {

  public HoldsToExpireSheet {
    Objects.requireNonNull(expiredHolds, "expiredHolds");
  }

  public List<ExpiredHold> getExpiredHolds() {
    return expiredHolds;
  }

  public Stream<PatronEvent.BookHoldExpired> toStreamOfEvents(Instant processingTime) {
    return expiredHolds.stream().map(expiredHold -> expiredHold.toEvent(processingTime));
  }

  public int count() {
    return expiredHolds.size();
  }
}
