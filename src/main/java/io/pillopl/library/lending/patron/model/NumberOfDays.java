package io.pillopl.library.lending.patron.model;

public final class NumberOfDays {

  private final int days;

  private NumberOfDays(int days) {
    if (days <= 0) {
      throw new IllegalArgumentException("Cannot use negative integer or zero as number of days");
    }
    this.days = days;
  }

  public static NumberOfDays of(int days) {
    return new NumberOfDays(days);
  }

  public int getDays() {
    return days;
  }

  boolean isGreaterThan(int days) {
    return this.days > days;
  }

  @Override
  public boolean equals(Object other) {
    return this == other || other instanceof NumberOfDays that && days == that.days;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(days);
  }

  @Override
  public String toString() {
    return "NumberOfDays(days=" + days + ")";
  }
}
