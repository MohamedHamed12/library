package io.pillopl.library.lending.patron.model;

import static io.pillopl.library.lending.patron.model.NumberOfDays.of;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class CheckoutDuration {

  static final int MAX_CHECKOUT_DURATION = 60;

  private final NumberOfDays noOfDays;
  private final Instant from;

  private CheckoutDuration(Instant from, NumberOfDays noOfDays) {
    this.from = Objects.requireNonNull(from, "from");
    this.noOfDays = Objects.requireNonNull(noOfDays, "noOfDays");
    if (noOfDays.isGreaterThan(MAX_CHECKOUT_DURATION)) {
      throw new IllegalArgumentException(
          "Cannot checkout for more than " + MAX_CHECKOUT_DURATION + " days!");
    }
  }

  public static CheckoutDuration forNoOfDays(Instant from, int noOfDays) {
    return new CheckoutDuration(from, of(noOfDays));
  }

  public static CheckoutDuration maxDuration(Instant from) {
    return forNoOfDays(from, MAX_CHECKOUT_DURATION);
  }

  public NumberOfDays getNoOfDays() {
    return noOfDays;
  }

  public Instant getFrom() {
    return from;
  }

  Instant to() {
    return from.plus(Duration.ofDays(noOfDays.getDays()));
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof CheckoutDuration that)) {
      return false;
    }
    return noOfDays.equals(that.noOfDays) && from.equals(that.from);
  }

  @Override
  public int hashCode() {
    return Objects.hash(noOfDays, from);
  }
}
