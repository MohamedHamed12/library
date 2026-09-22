package io.pillopl.library.lending.book.model;

import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.LendingEvent.BookPlacedOnHoldEvent;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

public final class AvailableBook implements Book {

  private final BookInformation bookInformation;
  private final LibraryBranchId libraryBranch;
  private final Version version;

  public AvailableBook(
      BookInformation bookInformation, LibraryBranchId libraryBranch, Version version) {
    this.bookInformation = Objects.requireNonNull(bookInformation, "bookInformation");
    this.libraryBranch = Objects.requireNonNull(libraryBranch, "libraryBranch");
    this.version = Objects.requireNonNull(version, "version");
  }

  public AvailableBook(
      BookId bookId, BookType type, LibraryBranchId libraryBranchId, Version version) {
    this(new BookInformation(bookId, type), libraryBranchId, version);
  }

  @Override
  public BookInformation getBookInformation() {
    return bookInformation;
  }

  public LibraryBranchId getLibraryBranch() {
    return libraryBranch;
  }

  @Override
  public Version getVersion() {
    return version;
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

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other instanceof AvailableBook that && bookInformation.equals(that.bookInformation);
  }

  @Override
  public int hashCode() {
    return bookInformation.hashCode();
  }
}
