package io.pillopl.library.lending.patron.application.hold;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.HoldDuration;
import io.pillopl.library.lending.patron.model.NumberOfDays;
import io.pillopl.library.lending.patron.model.PatronId;

public final class PlaceOnHoldCommand {

  private final Instant timestamp;
  private final PatronId patronId;
  private final LibraryBranchId libraryId;
  private final BookId bookId;
  private final Optional<Integer> noOfDays;

  public PlaceOnHoldCommand(
      Instant timestamp,
      PatronId patronId,
      LibraryBranchId libraryId,
      BookId bookId,
      Optional<Integer> noOfDays) {
    this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
    this.patronId = Objects.requireNonNull(patronId, "patronId");
    this.libraryId = Objects.requireNonNull(libraryId, "libraryId");
    this.bookId = Objects.requireNonNull(bookId, "bookId");
    this.noOfDays = Objects.requireNonNull(noOfDays, "noOfDays");
  }

  static PlaceOnHoldCommand closeEnded(
      Instant timestamp,
      PatronId patronId,
      LibraryBranchId libraryBranchId,
      BookId bookId,
      int forDays) {
    return new PlaceOnHoldCommand(
        timestamp, patronId, libraryBranchId, bookId, Optional.of(forDays));
  }

  static PlaceOnHoldCommand openEnded(
      Instant timestamp, PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId) {
    return new PlaceOnHoldCommand(
        timestamp, patronId, libraryBranchId, bookId, Optional.empty());
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

  public Optional<Integer> getNoOfDays() {
    return noOfDays;
  }

  HoldDuration getHoldDuration() {
    return noOfDays
        .map(NumberOfDays::of)
        .map(days -> HoldDuration.closeEnded(timestamp, days))
        .orElseGet(() -> HoldDuration.openEnded(timestamp));
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
        && noOfDays.equals(that.noOfDays);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, patronId, libraryId, bookId, noOfDays);
  }
}
