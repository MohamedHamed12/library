package io.pillopl.library.lending.dailysheet.model;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.patron.model.PatronId;

public record OverdueCheckout(
    BookId checkedOutBook, PatronId patron, LibraryBranchId library) {

  public OverdueCheckout {
    Objects.requireNonNull(checkedOutBook, "checkedOutBook");
    Objects.requireNonNull(patron, "patron");
    Objects.requireNonNull(library, "library");
  }

  public BookId getCheckedOutBook() {
    return checkedOutBook;
  }

  public PatronId getPatron() {
    return patron;
  }

  public LibraryBranchId getLibrary() {
    return library;
  }

  OverdueCheckoutRegistered toEvent(Instant processingTime) {
    return OverdueCheckoutRegistered.registeredAt(processingTime, patron, checkedOutBook, library);
  }
}
