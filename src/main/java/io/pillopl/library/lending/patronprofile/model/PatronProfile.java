package io.pillopl.library.lending.patronprofile.model;

import java.util.Objects;
import java.util.Optional;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.model.PatronStatus;

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

  public Optional<Hold> findHold(BookId bookId) {
    return holdsView.currentHolds().stream().filter(hold -> hold.book().equals(bookId)).findFirst();
  }

  public Optional<Checkout> findCheckout(BookId bookId) {
    return currentCheckouts.currentCheckouts().stream()
        .filter(checkout -> checkout.book().equals(bookId))
        .findFirst();
  }
}
