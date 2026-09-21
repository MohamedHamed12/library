package io.pillopl.library.lending.patron.model;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookOnHold;

record PatronHolds(Set<Hold> resourcesOnHold) {

  static int MAX_NUMBER_OF_HOLDS = 5;

  Set<Hold> getResourcesOnHold() {
    return resourcesOnHold;
  }

  boolean a(BookOnHold bookOnHold) {
    return find(Objects.requireNonNull(bookOnHold, "bookOnHold")).isPresent();
  }

  Optional<Hold> find(BookOnHold bookOnHold) {
    Objects.requireNonNull(bookOnHold, "bookOnHold");
    return resourcesOnHold.stream().filter(hold -> hold.matches(bookOnHold)).findFirst();
  }

  int count() {
    return resourcesOnHold.size();
  }

  boolean maximumHoldsAfterHolding(AvailableBook book) {
    return count() + 1 == MAX_NUMBER_OF_HOLDS;
  }
}
