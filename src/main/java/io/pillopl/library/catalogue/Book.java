package io.pillopl.library.catalogue;

import java.util.Objects;

final class Book {

  private final ISBN bookIsbn;
  private final Title title;
  private final Author author;

  Book(ISBN bookIsbn, Title title, Author author) {
    this.bookIsbn = Objects.requireNonNull(bookIsbn, "bookIsbn");
    this.title = Objects.requireNonNull(title, "title");
    this.author = Objects.requireNonNull(author, "author");
  }

  Book(String isbn, String author, String title) {
    this(new ISBN(isbn), new Title(title), new Author(author));
  }

  ISBN getBookIsbn() {
    return bookIsbn;
  }

  Title getTitle() {
    return title;
  }

  Author getAuthor() {
    return author;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Book that)) {
      return false;
    }
    return bookIsbn.equals(that.bookIsbn);
  }

  @Override
  public int hashCode() {
    return bookIsbn.hashCode();
  }

  @Override
  public String toString() {
    return "Book(bookIsbn=" + bookIsbn + ", title=" + title + ", author=" + author + ")";
  }
}

record Title(String title) {

  Title {
    Objects.requireNonNull(title, "title");
    if (title.isEmpty()) {
      throw new IllegalArgumentException("Title cannot be empty");
    }
    title = title.trim();
  }

  String getTitle() {
    return title;
  }
}

record Author(String name) {

  Author {
    Objects.requireNonNull(name, "name");
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Author cannot be empty");
    }
    name = name.trim();
  }

  String getName() {
    return name;
  }
}
