package io.pillopl.library.lending.patronprofile.model;

import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.model.PatronStatus;
import io.vavr.control.Option;

public record PatronProfile(
    PatronStatus status, HoldsView holdsView, CheckoutsView currentCheckouts) {

  public PatronProfile {
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(holdsView, "holdsView");
    Objects.requireNonNull(currentCheckouts, "currentCheckouts");
  }

  public PatronStatus getStatus() {
    return status;
  }

  public HoldsView getHoldsView() {
    return holdsView;
  }

  public CheckoutsView getCurrentCheckouts() {
    return currentCheckouts;
  }

  public Option<Hold> findHold(BookId bookId) {
    return holdsView
        .getCurrentHolds()
        .toStream()
        .find(hold -> hold.getBook().equals(bookId));
  }

  public Option<Checkout> findCheckout(BookId bookId) {
    return currentCheckouts
        .getCurrentCheckouts()
        .toStream()
        .find(checkout -> checkout.getBook().equals(bookId));
  }
}
