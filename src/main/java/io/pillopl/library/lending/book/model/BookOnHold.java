package io.pillopl.library.lending.book.model;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.LendingEvent.BookCheckedOutEvent;
import io.pillopl.library.lending.LendingEvent.BookHoldCanceledEvent;
import io.pillopl.library.lending.LendingEvent.BookHoldExpiredEvent;
import io.pillopl.library.lending.LendingEvent.BookHoldExtendedEvent;
import io.pillopl.library.lending.LendingEvent.BookReturnedEvent;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

public final class BookOnHold implements Book {

  private final BookInformation bookInformation;
  private final LibraryBranchId holdPlacedAt;
  private final PatronReference byPatron;
  private final Instant holdTill;
  private final Version version;

  BookOnHold(
      BookInformation bookInformation,
      LibraryBranchId holdPlacedAt,
      PatronReference byPatron,
      Instant holdTill,
      Version version) {
    this.bookInformation = Objects.requireNonNull(bookInformation, "bookInformation");
    this.holdPlacedAt = Objects.requireNonNull(holdPlacedAt, "holdPlacedAt");
    this.byPatron = Objects.requireNonNull(byPatron, "byPatron");
    this.holdTill = holdTill;
    this.version = Objects.requireNonNull(version, "version");
  }

  public BookOnHold(
      BookId bookId,
      BookType type,
      LibraryBranchId libraryBranchId,
      PatronReference patronId,
      Instant holdTill,
      Version version) {
    this(new BookInformation(bookId, type), libraryBranchId, patronId, holdTill, version);
  }

  @Override
  public BookInformation getBookInformation() {
    return bookInformation;
  }

  public LibraryBranchId getHoldPlacedAt() {
    return holdPlacedAt;
  }

  public PatronReference getByPatron() {
    return byPatron;
  }

  public Instant getHoldTill() {
    return holdTill;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  public AvailableBook handle(BookReturnedEvent bookReturned) {
    return new AvailableBook(
        bookInformation, new LibraryBranchId(bookReturned.getLibraryBranchId()), version);
  }

  public AvailableBook handle(BookHoldExpiredEvent bookHoldExpired) {
    return new AvailableBook(
        bookInformation, new LibraryBranchId(bookHoldExpired.getLibraryBranchId()), version);
  }

  public CheckedOutBook handle(BookCheckedOutEvent bookCheckedOut) {
    return new CheckedOutBook(
        bookInformation,
        new LibraryBranchId(bookCheckedOut.getLibraryBranchId()),
        PatronReference.of(bookCheckedOut.getPatronId()),
        version);
  }

  public AvailableBook handle(BookHoldCanceledEvent bookHoldCanceled) {
    return new AvailableBook(
        bookInformation, new LibraryBranchId(bookHoldCanceled.getLibraryBranchId()), version);
  }

  public BookOnHold handle(BookHoldExtendedEvent bookHoldExtended) {
    return new BookOnHold(
        bookInformation, holdPlacedAt, byPatron, bookHoldExtended.getHoldTill(), version);
  }

  public BookId getBookId() {
    return bookInformation.getBookId();
  }

  public boolean by(PatronReference patronId) {
    return byPatron.getPatronId().equals(patronId.getPatronId());
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other instanceof BookOnHold that && bookInformation.equals(that.bookInformation);
  }

  @Override
  public int hashCode() {
    return bookInformation.hashCode();
  }
}
