package io.pillopl.library.lending.patron.model;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

public record PatronHoldSnapshot(
    BookId bookId, LibraryBranchId libraryBranchId, Instant till, int extensionCount) {

  public PatronHoldSnapshot {
    Objects.requireNonNull(bookId, "bookId");
    Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    if (extensionCount < 0) {
      throw new IllegalArgumentException("Extension count cannot be negative");
    }
  }
}
