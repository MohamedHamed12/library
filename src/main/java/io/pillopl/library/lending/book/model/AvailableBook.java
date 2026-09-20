package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.LendingEvent.BookPlacedOnHoldEvent;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor
@EqualsAndHashCode(of = "bookInformation")
public final class AvailableBook implements Book {

  @NonNull BookInformation bookInformation;

  @NonNull LibraryBranchId libraryBranch;

  @NonNull Version version;

  public AvailableBook(
      BookId bookId, BookType type, LibraryBranchId libraryBranchId, Version version) {
    this(new BookInformation(bookId, type), libraryBranchId, version);
  }

  public boolean isRestricted() {
    return bookInformation.getBookType().equals(BookType.Restricted);
  }

  public BookId getBookId() {
    return bookInformation.getBookId();
  }

  public BookOnHold handle(BookPlacedOnHoldEvent bookPlacedOnHold) {
    return new BookOnHold(
        bookInformation,
        new LibraryBranchId(bookPlacedOnHold.getLibraryBranchId()),
        PatronReference.of(bookPlacedOnHold.getPatronId()),
        bookPlacedOnHold.getHoldTill(),
        version);
  }
}
