package io.pillopl.library.lending.book.model;

import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.LendingEvent.BookReturnedEvent;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

public final class CheckedOutBook implements Book {

  private final BookInformation bookInformation;
  private final LibraryBranchId checkedOutAt;
  private final PatronReference byPatron;
  private final Version version;

  CheckedOutBook(
      BookInformation bookInformation,
      LibraryBranchId checkedOutAt,
      PatronReference byPatron,
      Version version) {
    this.bookInformation = Objects.requireNonNull(bookInformation, "bookInformation");
    this.checkedOutAt = Objects.requireNonNull(checkedOutAt, "checkedOutAt");
    this.byPatron = Objects.requireNonNull(byPatron, "byPatron");
    this.version = Objects.requireNonNull(version, "version");
  }

  public CheckedOutBook(
      BookId bookId,
      BookType type,
      LibraryBranchId libraryBranchId,
      PatronReference patronId,
      Version version) {
    this(new BookInformation(bookId, type), libraryBranchId, patronId, version);
  }

  @Override
  public BookInformation getBookInformation() {
    return bookInformation;
  }

  public LibraryBranchId getCheckedOutAt() {
    return checkedOutAt;
  }

  public PatronReference getByPatron() {
    return byPatron;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  public BookId getBookId() {
    return bookInformation.getBookId();
  }

  public AvailableBook handle(BookReturnedEvent bookReturnedByPatron) {
    return new AvailableBook(
        bookInformation, new LibraryBranchId(bookReturnedByPatron.getLibraryBranchId()), version);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other instanceof CheckedOutBook that && bookInformation.equals(that.bookInformation);
  }

  @Override
  public int hashCode() {
    return bookInformation.hashCode();
  }
}
