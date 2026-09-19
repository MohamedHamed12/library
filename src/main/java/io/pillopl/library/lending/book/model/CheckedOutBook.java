package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.LendingEvent.BookReturnedEvent;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "bookInformation")
public class CheckedOutBook implements Book {

  @NonNull BookInformation bookInformation;

  @NonNull LibraryBranchId checkedOutAt;

  @NonNull PatronReference byPatron;

  @NonNull Version version;

  public CheckedOutBook(
      BookId bookId,
      BookType type,
      LibraryBranchId libraryBranchId,
      PatronReference patronId,
      Version version) {
    this(new BookInformation(bookId, type), libraryBranchId, patronId, version);
  }

  public BookId getBookId() {
    return bookInformation.getBookId();
  }

  public AvailableBook handle(BookReturnedEvent bookReturnedByPatron) {
    return new AvailableBook(
        bookInformation, new LibraryBranchId(bookReturnedByPatron.getLibraryBranchId()), version);
  }
}
