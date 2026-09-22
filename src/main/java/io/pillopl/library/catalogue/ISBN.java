package io.pillopl.library.catalogue;

import java.util.Objects;

record ISBN(String isbn) {

  private static final String VERY_SIMPLE_ISBN_CHECK = "^\\d{9}[\\d|X]$";

  ISBN {
    Objects.requireNonNull(isbn, "isbn");
    if (!isbn.trim().matches(VERY_SIMPLE_ISBN_CHECK)) {
      throw new IllegalArgumentException("Wrong ISBN!");
    }
    isbn = isbn.trim();
  }

  String getIsbn() {
    return isbn;
  }
}
