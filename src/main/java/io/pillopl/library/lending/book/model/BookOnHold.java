package io.pillopl.library.lending.book.model;

import java.time.Instant;

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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "bookInformation")
public class BookOnHold implements Book {

  @NonNull BookInformation bookInformation;

  @NonNull LibraryBranchId holdPlacedAt;

  @NonNull PatronReference byPatron;

  Instant holdTill;

  @NonNull Version version;

  public BookOnHold(
      BookId bookId,
      BookType type,
      LibraryBranchId libraryBranchId,
      PatronReference patronId,
      Instant holdTill,
      Version version) {
    this(new BookInformation(bookId, type), libraryBranchId, patronId, holdTill, version);
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
}
