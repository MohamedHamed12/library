package io.pillopl.library.lending.book.model;

import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;

public record BookInformation(BookId bookId, BookType bookType) {

  public BookInformation {
    Objects.requireNonNull(bookId, "bookId");
    Objects.requireNonNull(bookType, "bookType");
  }

  public BookId getBookId() {
    return bookId;
  }

  public BookType getBookType() {
    return bookType;
  }
}
