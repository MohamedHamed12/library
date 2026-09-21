package io.pillopl.library.lending.dailysheet.model;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExpired;
import io.pillopl.library.lending.patron.model.PatronId;

public record ExpiredHold(BookId heldBook, PatronId patron, LibraryBranchId library) {

  public ExpiredHold {
    Objects.requireNonNull(heldBook, "heldBook");
    Objects.requireNonNull(patron, "patron");
    Objects.requireNonNull(library, "library");
  }

  public BookId getHeldBook() {
    return heldBook;
  }

  public PatronId getPatron() {
    return patron;
  }

  public LibraryBranchId getLibrary() {
    return library;
  }

  BookHoldExpired toEvent(Instant processingTime) {
    return BookHoldExpired.expiredAt(processingTime, heldBook, patron, library);
  }
}
