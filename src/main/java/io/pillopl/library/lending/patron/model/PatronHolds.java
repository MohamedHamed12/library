package io.pillopl.library.lending.patron.model;

import java.util.Objects;
import java.util.Set;

import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.vavr.control.Option;

record PatronHolds(Set<Hold> resourcesOnHold) {

  static int MAX_NUMBER_OF_HOLDS = 5;

  Set<Hold> getResourcesOnHold() {
    return resourcesOnHold;
  }

  boolean a(BookOnHold bookOnHold) {
    Objects.requireNonNull(bookOnHold, "bookOnHold");
    return find(bookOnHold).isDefined();
  }

  Option<Hold> find(BookOnHold bookOnHold) {
    Objects.requireNonNull(bookOnHold, "bookOnHold");
    return Option.of(
        resourcesOnHold.stream().filter(hold -> hold.matches(bookOnHold)).findFirst().orElse(null));
  }

  int count() {
    return resourcesOnHold.size();
  }

  boolean maximumHoldsAfterHolding(AvailableBook book) {
    return count() + 1 == MAX_NUMBER_OF_HOLDS;
  }
}
