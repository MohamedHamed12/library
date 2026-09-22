package io.pillopl.library.lending.patronprofile.model;

import java.time.Instant;

import io.pillopl.library.catalogue.BookId;

public record Checkout(BookId book, Instant till) {

  public BookId getBook() {
    return book;
  }

  public Instant getTill() {
    return till;
  }
}
