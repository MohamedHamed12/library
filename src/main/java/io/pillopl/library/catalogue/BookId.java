package io.pillopl.library.catalogue;

import java.util.Objects;
import java.util.UUID;

public record BookId(UUID bookId) {

  public BookId {
    Objects.requireNonNull(bookId, "bookId");
  }

  public UUID getBookId() {
    return bookId;
  }
}
