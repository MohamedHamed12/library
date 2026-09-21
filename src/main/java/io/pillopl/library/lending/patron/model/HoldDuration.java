package io.pillopl.library.lending.patron.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import io.vavr.control.Option;

public final class HoldDuration {

  private final Instant from;
  private final Instant to;

  private HoldDuration(Instant from, Instant to) {
    if (from == null) {
      throw new IllegalArgumentException("Hold duration start time cannot be null");
    }
    if (to != null && to.isBefore(from)) {
      throw new IllegalStateException("Close-ended duration must be valid");
    }
    this.from = from;
    this.to = to;
  }

  public Instant getFrom() {
    return from;
  }

  boolean isOpenEnded() {
    return getTo().isEmpty();
  }

  Option<Instant> getTo() {
    return Option.of(to);
  }

  public static HoldDuration openEnded(Instant from) {
    return new HoldDuration(from, null);
  }

  public static HoldDuration closeEnded(Instant from, NumberOfDays days) {
    Instant till = from.plus(Duration.ofDays(days.getDays()));
    return new HoldDuration(from, till);
  }

  public static HoldDuration closeEnded(Instant from, int days) {
    return closeEnded(from, NumberOfDays.of(days));
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof HoldDuration that)) {
      return false;
    }
    return from.equals(that.from) && Objects.equals(to, that.to);
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to);
  }
}
