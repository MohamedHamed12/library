package io.pillopl.library.catalogue;

import java.util.Objects;
import java.util.UUID;

final class BookInstance {

  private final ISBN bookIsbn;
  private final BookId bookId;
  private final BookType bookType;

  private BookInstance(ISBN bookIsbn, BookId bookId, BookType bookType) {
    this.bookIsbn = Objects.requireNonNull(bookIsbn, "bookIsbn");
    this.bookId = Objects.requireNonNull(bookId, "bookId");
    this.bookType = Objects.requireNonNull(bookType, "bookType");
  }

  static BookInstance instanceOf(Book book, BookType bookType) {
    return new BookInstance(book.getBookIsbn(), new BookId(UUID.randomUUID()), bookType);
  }

  ISBN getBookIsbn() {
    return bookIsbn;
  }

  BookId getBookId() {
    return bookId;
  }

  BookType getBookType() {
    return bookType;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof BookInstance that)) {
      return false;
    }
    return bookIsbn.equals(that.bookIsbn)
        && bookId.equals(that.bookId)
        && bookType.equals(that.bookType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bookIsbn, bookId, bookType);
  }

  @Override
  public String toString() {
    return "BookInstance(bookIsbn="
        + bookIsbn
        + ", bookId="
        + bookId
        + ", bookType="
        + bookType
        + ")";
  }
}
