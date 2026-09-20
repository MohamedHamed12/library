package io.pillopl.library.lending.patron.application.hold;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.model.NumberOfDays;
import io.pillopl.library.lending.patron.model.PatronId;

public record ExtendHoldCommand(
    Instant timestamp, PatronId patronId, BookId bookId, NumberOfDays additionalDays) {

  public ExtendHoldCommand {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(patronId, "patronId");
    Objects.requireNonNull(bookId, "bookId");
    Objects.requireNonNull(additionalDays, "additionalDays");
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public PatronId getPatronId() {
    return patronId;
  }

  public BookId getBookId() {
    return bookId;
  }

  public NumberOfDays getAdditionalDays() {
    return additionalDays;
  }
}
