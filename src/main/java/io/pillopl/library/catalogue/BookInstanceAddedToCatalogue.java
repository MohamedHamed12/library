package io.pillopl.library.catalogue;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import io.pillopl.library.commons.events.DomainEvent;

public final class BookInstanceAddedToCatalogue implements DomainEvent {

  private final UUID eventId = UUID.randomUUID();
  private final String isbn;
  private final BookType type;
  private final UUID bookId;
  private final Instant when;

  BookInstanceAddedToCatalogue(String isbn, BookType type, UUID bookId, Instant when) {
    this.isbn = Objects.requireNonNull(isbn, "isbn");
    this.type = Objects.requireNonNull(type, "type");
    this.bookId = Objects.requireNonNull(bookId, "bookId");
    this.when = Objects.requireNonNull(when, "when");
  }

  static BookInstanceAddedToCatalogue addedAt(Instant timestamp, BookInstance bookInstance) {
    return new BookInstanceAddedToCatalogue(
        bookInstance.getBookIsbn().getIsbn(),
        bookInstance.getBookType(),
        bookInstance.getBookId().getBookId(),
        timestamp);
  }

  @Override
  public UUID getEventId() {
    return eventId;
  }

  public String getIsbn() {
    return isbn;
  }

  public BookType getType() {
    return type;
  }

  public UUID getBookId() {
    return bookId;
  }

  @Override
  public Instant getWhen() {
    return when;
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
    if (!(other instanceof BookInstanceAddedToCatalogue that)) {
      return false;
    }
    return eventId.equals(that.eventId)
        && isbn.equals(that.isbn)
        && type.equals(that.type)
        && bookId.equals(that.bookId)
        && when.equals(that.when);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, isbn, type, bookId, when);
  }
}
