package io.pillopl.library.lending.book.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import io.pillopl.library.commons.events.DomainEvent;

public final class BookDuplicateHoldFound implements DomainEvent {

  private final UUID eventId = UUID.randomUUID();
  private final Instant when;
  private final UUID firstPatronId;
  private final UUID secondPatronId;
  private final UUID libraryBranchId;
  private final UUID bookId;

  public BookDuplicateHoldFound(
      Instant when,
      UUID firstPatronId,
      UUID secondPatronId,
      UUID libraryBranchId,
      UUID bookId) {
    this.when = Objects.requireNonNull(when, "when");
    this.firstPatronId = Objects.requireNonNull(firstPatronId, "firstPatronId");
    this.secondPatronId = Objects.requireNonNull(secondPatronId, "secondPatronId");
    this.libraryBranchId = Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    this.bookId = Objects.requireNonNull(bookId, "bookId");
  }

  @Override
  public UUID getEventId() {
    return eventId;
  }

  @Override
  public Instant getWhen() {
    return when;
  }

  public UUID getFirstPatronId() {
    return firstPatronId;
  }

  public UUID getSecondPatronId() {
    return secondPatronId;
  }

  public UUID getLibraryBranchId() {
    return libraryBranchId;
  }

  public UUID getBookId() {
    return bookId;
  }

  @Override
  public UUID getAggregateId() {
    return bookId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof BookDuplicateHoldFound that)) {
      return false;
    }
    return eventId.equals(that.eventId)
        && when.equals(that.when)
        && firstPatronId.equals(that.firstPatronId)
        && secondPatronId.equals(that.secondPatronId)
        && libraryBranchId.equals(that.libraryBranchId)
        && bookId.equals(that.bookId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, when, firstPatronId, secondPatronId, libraryBranchId, bookId);
  }
}
