package io.pillopl.library.lending.patron.application.checkout;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.CheckoutDuration;
import io.pillopl.library.lending.patron.model.PatronId;

public record CheckOutBookCommand(
    Instant timestamp,
    PatronId patronId,
    LibraryBranchId libraryId,
    BookId bookId,
    Integer noOfDays) {

  public CheckOutBookCommand {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(patronId, "patronId");
    Objects.requireNonNull(libraryId, "libraryId");
    Objects.requireNonNull(bookId, "bookId");
    Objects.requireNonNull(noOfDays, "noOfDays");
  }

  public static CheckOutBookCommand create(
      Instant timestamp,
      PatronId patronId,
      LibraryBranchId libraryBranchId,
      BookId bookId,
      int noOfDays) {
    return new CheckOutBookCommand(timestamp, patronId, libraryBranchId, bookId, noOfDays);
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

  public Integer getNoOfDays() {
    return noOfDays;
  }

  CheckoutDuration getCheckoutDuration() {
    return CheckoutDuration.forNoOfDays(timestamp, noOfDays);
  }
}
