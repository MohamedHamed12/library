package io.pillopl.library.lending.patron.application.hold;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.HoldDuration;
import io.pillopl.library.lending.patron.model.NumberOfDays;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;

public final class PlaceOnHoldCommand {

  private final Instant timestamp;
  private final PatronId patronId;
  private final LibraryBranchId libraryId;
  private final BookId bookId;
  private final Option<Integer> noOfDays;

  public PlaceOnHoldCommand(
      Instant timestamp,
      PatronId patronId,
      LibraryBranchId libraryId,
      BookId bookId,
      Option<Integer> noOfDays) {
    this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
    this.patronId = Objects.requireNonNull(patronId, "patronId");
    this.libraryId = Objects.requireNonNull(libraryId, "libraryId");
    this.bookId = Objects.requireNonNull(bookId, "bookId");
    this.noOfDays = noOfDays;
  }

  static PlaceOnHoldCommand closeEnded(
      Instant timestamp,
      PatronId patronId,
      LibraryBranchId libraryBranchId,
      BookId bookId,
      int forDays) {
    return new PlaceOnHoldCommand(
        timestamp, patronId, libraryBranchId, bookId, Option.of(forDays));
  }

  static PlaceOnHoldCommand openEnded(
      Instant timestamp, PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId) {
    return new PlaceOnHoldCommand(timestamp, patronId, libraryBranchId, bookId, Option.none());
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public PatronId getPatronId() {
    return patronId;
  }

  public LibraryBranchId getLibraryId() {
    return libraryId;
  }

  public BookId getBookId() {
    return bookId;
  }

  public Option<Integer> getNoOfDays() {
    return noOfDays;
  }

  HoldDuration getHoldDuration() {
    return noOfDays
        .map(NumberOfDays::of)
        .map(days -> HoldDuration.closeEnded(timestamp, days))
        .getOrElse(() -> HoldDuration.openEnded(timestamp));
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PlaceOnHoldCommand that)) {
      return false;
    }
    return timestamp.equals(that.timestamp)
        && patronId.equals(that.patronId)
        && libraryId.equals(that.libraryId)
        && bookId.equals(that.bookId)
        && Objects.equals(noOfDays, that.noOfDays);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, patronId, libraryId, bookId, noOfDays);
  }
}
