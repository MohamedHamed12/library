package io.pillopl.library.lending.patron.model;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

record Hold(BookId bookId, LibraryBranchId libraryBranchId, Instant till, int extensionCount) {

  Hold {
    Objects.requireNonNull(bookId, "bookId");
    Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    if (extensionCount < 0) {
      throw new IllegalArgumentException("Extension count cannot be negative");
    }
  }

  Hold(BookId bookId, LibraryBranchId libraryBranchId) {
    this(bookId, libraryBranchId, null, 0);
  }

  BookId getBookId() {
    return bookId;
  }

  LibraryBranchId getLibraryBranchId() {
    return libraryBranchId;
  }

  Instant getTill() {
    return till;
  }

  int getExtensionCount() {
    return extensionCount;
  }

  boolean matches(BookOnHold bookOnHold) {
    Objects.requireNonNull(bookOnHold, "bookOnHold");
    return bookId.equals(bookOnHold.getBookId())
        && libraryBranchId.equals(bookOnHold.getHoldPlacedAt());
  }

  boolean isOpenEnded() {
    return till == null;
  }

  boolean isCurrentAt(Instant timestamp) {
    Objects.requireNonNull(timestamp, "timestamp");
    return till != null && timestamp.isBefore(till);
  }
}
